package com.friday.commerce.catalog.application.dto.product.response;

public record ProductBriefResponse(
        Long productId,
        String productTitle,
        Long price,
        Long stock,
        String status
) {}