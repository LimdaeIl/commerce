package com.friday.commerce.order.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "주문상품: 필수입니다.")
        List<CreateOrderItemRequest> items,

        @NotNull(message = "주문배송지: 필수입니다.")
        DeliveryAddressRequest deliveryAddress
) {

}
