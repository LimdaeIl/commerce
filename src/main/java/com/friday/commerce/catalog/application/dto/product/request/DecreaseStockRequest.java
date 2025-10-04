package com.friday.commerce.catalog.application.dto.product.request;

import jakarta.validation.constraints.Positive;

public record DecreaseStockRequest(
        @Positive(message = "수량: 수량은 양수이어야만 합니다.")
        int quantity
) {

}
