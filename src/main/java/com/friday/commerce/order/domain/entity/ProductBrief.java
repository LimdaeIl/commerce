package com.friday.commerce.order.domain.entity;

public record ProductBrief(
        Long productId,
        String productTitle,
        Long price,
        Long stock
) {

}

