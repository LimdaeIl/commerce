package com.friday.commerce.payment.infrastructure.toss;

import com.friday.commerce.payment.application.port.out.OrderPort;
import com.friday.commerce.payment.domain.entity.Payment;
import com.friday.commerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@Component
public class PaymentWriter {

    private final TransactionTemplate transactionTemplate;
    private final PaymentRepository paymentRepository;
    private final OrderPort orderPort;

    @Transactional
    public Payment saveApprovedAndMarkOrderPaid(Payment pay, Long orderId, Long userId) {
        try {
            Payment saved = paymentRepository.save(pay);
            orderPort.markPaid(orderId, userId); // 같은 트랜잭션에서 상태 전이
            return saved;
        } catch (DataIntegrityViolationException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return transactionTemplate.execute(status ->
                    paymentRepository.findByPaymentKey(pay.getPaymentKey())
                            .orElseThrow(() -> e)
            );
        }
    }
}
