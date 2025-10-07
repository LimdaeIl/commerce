package com.friday.commerce.payment.infrastructure.toss;

import com.friday.commerce.payment.application.port.out.OrderPort;
import com.friday.commerce.payment.domain.entity.Payment;
import com.friday.commerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentWriter {

    private final PaymentRepository paymentRepository;
    private final OrderPort orderPort;

    @Transactional
    public Payment saveApprovedAndMarkOrderPaid(Payment pay, Long orderId, Long actorId) {
        try {
            Payment saved = paymentRepository.save(pay);
            orderPort.markPaid(orderId, actorId); // 같은 트랜잭션에서 상태 전이
            return saved;
        } catch (DataIntegrityViolationException e) {
            // payment_key 유니크 충돌 시 멱등 처리
            return paymentRepository.findByPaymentKey(pay.getPaymentKey())
                    .orElseThrow(() -> e);
        }
    }
}
