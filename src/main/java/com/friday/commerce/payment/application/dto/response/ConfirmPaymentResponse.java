package com.friday.commerce.payment.application.dto.response;

import com.friday.commerce.payment.domain.entity.Payment;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ConfirmPaymentResponse(
        String paymentKey,
        Long orderId,
        Integer totalAmount,
        String method,
        String paymentStatus,
        LocalDateTime approvedAt
) {

    public static ConfirmPaymentResponse of(Payment payment) {
        return ConfirmPaymentResponse.builder()
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrderId())
                .totalAmount(payment.getTotalAmount())
                .method(payment.getMethod())
                .paymentStatus(payment.getPaymentStatus().name())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}
