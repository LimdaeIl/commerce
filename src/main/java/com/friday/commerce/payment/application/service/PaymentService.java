package com.friday.commerce.payment.application.service;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.payment.application.dto.request.ConfirmPaymentRequest;
import com.friday.commerce.payment.application.dto.response.ConfirmPaymentResponse;
import com.friday.commerce.payment.application.port.out.OrderPort;
import com.friday.commerce.payment.application.usecase.PaymentUseCase;
import com.friday.commerce.payment.domain.entity.OrderBrief;
import com.friday.commerce.payment.domain.entity.Payment;
import com.friday.commerce.payment.domain.entity.PaymentStatus;
import com.friday.commerce.payment.domain.exception.PaymentErrorCode;
import com.friday.commerce.payment.domain.exception.PaymentException;
import com.friday.commerce.payment.domain.repository.PaymentRepository;
import com.friday.commerce.payment.infrastructure.toss.PaymentWriter;
import com.friday.commerce.payment.infrastructure.toss.TossPaymentsClient;
import com.friday.commerce.payment.infrastructure.toss.dto.TossConfirmResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "PaymentService")
@RequiredArgsConstructor
@Service
public class PaymentService implements PaymentUseCase {

    private final Snowflake snowflake;
    private final PaymentRepository paymentRepository;
    private final OrderPort orderPort;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentWriter paymentWriter;


    @Override
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest request, CurrentUserInfo info) {
        // 멱등 fast-path
        var existing = paymentRepository.findByPaymentKey(request.paymentKey());
        if (existing.isPresent()) {
            return ConfirmPaymentResponse.of(existing.get());
        }

        // 주문 검증
        OrderBrief orderBrief = orderPort.findByOrderId(request.orderId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));
        int totalAmount = orderBrief.totalAmount();

        log.info("orderBrief.totalAmount(): {}", orderBrief.totalAmount());

        if (!"CREATED".equals(orderBrief.status())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // PG 승인
        String idemKey = "confirm:%s:%s".formatted(request.orderId(), request.paymentKey());
        TossConfirmResponse pg = tossPaymentsClient.confirm(
                request.paymentKey(),
                String.valueOf(request.orderId()),
                totalAmount,
                idemKey
        );

        // PG 응답 검증
        log.info("pg.totalAmount(): {}", pg.totalAmount());
        log.info("totalAmount: {}", totalAmount);
        if (!"DONE".equalsIgnoreCase(pg.status())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        if (pg.totalAmount() != null && !pg.totalAmount().equals(totalAmount)) {
            tossPaymentsClient.cancel(request.paymentKey(), "amount_mismatch", pg.totalAmount());
            throw new PaymentException(PaymentErrorCode.ORDER_AMOUNT_MISMATCH);
        }
        if (pg.orderId() != null && !pg.orderId().equals(String.valueOf(request.orderId()))) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 도메인 생성/전이
        LocalDateTime approvedAt =
                (pg.approvedAt() == null) ? null : pg.approvedAt().toLocalDateTime();

        Payment payment = Payment.create(
                snowflake.nextId(),
                request.paymentKey(),
                request.orderId(),
                totalAmount,
                approvedAt
        );

        payment.markApproved(pg.method() != null ? pg.method() : "CARD",
                PaymentStatus.DONE, approvedAt);

        Long actorId = (info != null ? info.userId() : orderBrief.userId());

        // 트랜잭션 내 저장 + 주문 PAID 전이
        Payment saved = paymentWriter.saveApprovedAndMarkOrderPaid(payment, request.orderId(),
                actorId);

        // 여기서 기존의 응답 DTO를 그대로 사용
        return ConfirmPaymentResponse.of(saved);
    }
}
