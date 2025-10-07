package com.friday.commerce.payment.domain.entity;

public record OrderBrief(
        Long orderId,
        Long userId,
        Integer totalAmount,
        String status
) {}
