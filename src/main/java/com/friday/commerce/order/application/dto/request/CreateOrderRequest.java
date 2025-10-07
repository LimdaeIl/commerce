package com.friday.commerce.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(

        @Valid
        @NotNull(message = "주문상품: 필수입니다.")
        @NotEmpty(message = "주문상품: 최소 1개 이상이어야 합니다.")
        List<CreateOrderItemRequest> items,

        @Valid
        @NotNull(message = "주문배송지: 필수입니다.")
        DeliveryAddressRequest deliveryAddress
) {

}
