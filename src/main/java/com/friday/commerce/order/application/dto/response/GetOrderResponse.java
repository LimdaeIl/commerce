package com.friday.commerce.order.application.dto.response;


import java.time.LocalDateTime;

public record GetOrderResponse(
        Long orderId,
        String status,
        Integer totalAmount,
        Long itemCount,
        LocalDateTime createdAt,
        String primaryProductName,
        String primaryImageUrl
) {}
