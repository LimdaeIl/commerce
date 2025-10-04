package com.friday.commerce.catalog.application.dto.product.response;


import com.friday.commerce.catalog.domain.entity.ProductStatus;

public record GetAllProductsResponse(
        Long productId,
        String title,
        ProductStatus status,
        Long minPrice,      // SKU 최소가
        String thumbnailUrl // sortOrder=0 이미지
) { }
