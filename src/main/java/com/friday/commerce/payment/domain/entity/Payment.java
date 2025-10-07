package com.friday.commerce.payment.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "payment_key", nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(name = "order_id", nullable = false, length = 64)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "method", length = 40)
    private String method;

    @Column(name = "status", length = 40)
    private PaymentStatus paymentStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Payment(
            Long id,
            String paymentKey,
            Long orderId,
            Integer totalAmount,
            LocalDateTime approvedAt
            ) {
        this.paymentId = id;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.approvedAt = approvedAt;
    }

    public static Payment create(
            Long paymentId,
            String paymentKey,
            Long orderId,
            Integer amount,
            LocalDateTime approvedAt
            ) {
        return new Payment(
                paymentId,
                paymentKey,
                orderId,
                amount,
                approvedAt);
    }

    public void markApproved(
            String method,
            PaymentStatus paymentStatus,
            LocalDateTime approvedAt) {
        this.method = method;
        this.paymentStatus = paymentStatus;
        this.approvedAt = approvedAt;
    }

    public void markCanceled(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
