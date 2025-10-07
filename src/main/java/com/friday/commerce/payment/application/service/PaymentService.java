package com.friday.commerce.payment.application.service;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.payment.application.dto.request.ConfirmPaymentRequest;
import com.friday.commerce.payment.application.dto.response.ConfirmPaymentResponse;
import com.friday.commerce.payment.application.port.out.OrderPort;
import com.friday.commerce.payment.application.usecase.PaymentUseCase;
import com.friday.commerce.payment.domain.entity.Payment;
import com.friday.commerce.payment.domain.entity.PaymentStatus;
import com.friday.commerce.payment.domain.exception.PaymentErrorCode;
import com.friday.commerce.payment.domain.exception.PaymentException;
import com.friday.commerce.payment.domain.repository.PaymentRepository;
import com.friday.commerce.payment.infrastructure.toss.PaymentWriter;
import com.friday.commerce.payment.infrastructure.toss.TossPaymentsClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentService implements PaymentUseCase {

    private final Snowflake snowflake;
    private final PaymentRepository paymentRepository;
    private final OrderPort orderPort;
    private final TossPaymentsClient toss;
    private final PaymentWriter paymentWriter;

    @Override
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest request, CurrentUserInfo info) {
        // 0) 멱등 Fast-Path (트랜잭션 없이)
        var existing = paymentRepository.findByPaymentKey(request.paymentKey());
        if (existing.isPresent()) {
            return ConfirmPaymentResponse.of(existing.get());
        }

        // 1) 주문 검증 (트랜잭션 밖)
        var ob = orderPort.findByOrderId(request.orderId())
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!ob.totalAmount().equals(request.amount())) {
            throw new PaymentException(PaymentErrorCode.ORDER_AMOUNT_MISMATCH);
        }
        if (!"CREATED".equals(ob.status())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 2) Toss 승인 호출 (트랜잭션 밖)
        var idemKey = "confirm:" + request.orderId() + ":" + request.paymentKey();
        Map<String, Object> res;
        try {
            res = toss.confirm(request.paymentKey(), String.valueOf(request.orderId()),
                    request.amount(), idemKey);
        } catch (Exception e) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 3) PG 응답 검증
        String status = asString(res.get("status"));             // 기대: "DONE"
        String method = asString(res.get("method"));             // 예: "CARD"
        Integer paidAmount = asInt(res.get("totalAmount"), res.get("amount")); // 응답 키 케이스 대비
        String orderIdEcho = asString(res.get("orderId"));
        String approvedAtIso = asString(res.get("approvedAt"));  // ISO-8601

        if (!"DONE".equalsIgnoreCase(status)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        if (paidAmount != null && !paidAmount.equals(request.amount())) {
            throw new PaymentException(PaymentErrorCode.ORDER_AMOUNT_MISMATCH);
        }
        if (orderIdEcho != null && !orderIdEcho.equals(String.valueOf(request.orderId()))) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 4) 결제 엔티티 구성 (approvedAt 파싱)
        var approvedAt = parseIsoDateTime(approvedAtIso); // null 허용
        Payment pay = Payment.pending(
                snowflake.nextId(),
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );
        pay.markApproved(method != null ? method : "CARD", PaymentStatus.DONE);

        // ✅ actorId 보정
        Long actorId = (info != null ? info.userId() : ob.userId());

        // ✅ 저장 + 주문 PAID 전이
        Payment saved = paymentWriter.saveApprovedAndMarkOrderPaid(pay, request.orderId(), actorId);

        return ConfirmPaymentResponse.of(saved);
    }

    // ---- helpers ----
    private static String asString(Object o) {
        return o instanceof String s ? s : null;
    }

    private static Integer asInt(Object... candidates) {
        for (Object o : candidates) {
            if (o instanceof Integer i) {
                return i;
            }
            if (o instanceof Number n) {
                return n.intValue();
            }
            if (o instanceof String s && s.matches("\\d+")) {
                return Integer.parseInt(s);
            }
        }
        return null;
    }

    private static java.time.LocalDateTime parseIsoDateTime(String iso) {
        if (iso == null) {
            return null;
        }
        try {
            return java.time.OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (Exception ignore) {
            try {
                return java.time.LocalDateTime.parse(iso);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
