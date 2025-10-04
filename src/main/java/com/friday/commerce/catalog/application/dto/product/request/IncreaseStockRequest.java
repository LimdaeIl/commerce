package com.friday.commerce.catalog.application.dto.product.request;

import jakarta.validation.constraints.PositiveOrZero;

public record IncreaseStockRequest(

        @PositiveOrZero
        int quantity

) {

}
