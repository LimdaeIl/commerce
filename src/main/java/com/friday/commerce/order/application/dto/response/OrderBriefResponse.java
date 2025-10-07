package com.friday.commerce.order.application.dto.response;

import com.friday.commerce.order.domain.entity.OrderStatus;

public record OrderBriefResponse(
        Long orderId,
        Long userId,
        Integer totalAmount,
        OrderStatus status
) {}
