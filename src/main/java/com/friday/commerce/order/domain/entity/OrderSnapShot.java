package com.friday.commerce.order.domain.entity;

public record OrderSnapShot(
        Long productId,
        String productTitle,
        Long price,
        int quantity,
        String recipientName,
        String ordererEmail,
        String zipCode,
        String addressLine1,
        String addressLine2,
        String city,
        String state
) {

}
