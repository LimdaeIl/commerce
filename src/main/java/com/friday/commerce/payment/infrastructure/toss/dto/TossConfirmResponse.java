package com.friday.commerce.payment.infrastructure.toss.dto;

import java.time.OffsetDateTime;

public record TossConfirmResponse(
        String status,
        String method,
        Integer totalAmount,
        String orderId,
        OffsetDateTime approvedAt
) {

}