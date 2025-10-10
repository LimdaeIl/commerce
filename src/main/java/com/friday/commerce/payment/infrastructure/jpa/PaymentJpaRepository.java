package com.friday.commerce.payment.infrastructure.jpa;

import com.friday.commerce.payment.domain.entity.Payment;
import com.friday.commerce.payment.domain.repository.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {

}
