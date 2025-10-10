package com.friday.commerce.order.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(

        @NotNull(message = "상품ID: 필수입니다.")
        Long productId,

        @NotNull(message = "상품주문수량: 필수입니다.")
        @Min(value = 0, message = "상품주문수량: 0 이상이어야 합니다.")
        int quantity
) {

}
