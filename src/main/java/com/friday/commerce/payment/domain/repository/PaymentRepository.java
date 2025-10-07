package com.friday.commerce.payment.domain.repository;

import com.friday.commerce.payment.domain.entity.Payment;
import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findByPaymentKey(String paymentKey);

    Payment save(Payment payment);
}
