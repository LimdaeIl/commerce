package com.friday.commerce.order.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.order.domain.entity.Order;
import com.friday.commerce.order.domain.entity.OrderStatus;
import com.friday.commerce.order.domain.entity.OrdererAddress;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateOrderResponse(
        Long orderId,
        Long userId,
        OrderStatus status,
        Integer totalAmount,
        LocalDateTime createdAt,
        Address address,
        List<Item> items
) {
    public record Item(
            Long productId,
            String productName,
            Integer unitPrice,
            Integer quantity,
            Integer lineTotal
    ) {}

    public record Address(
            String ordererName,
            String ordererEmail,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state
    ) {}

    public static CreateOrderResponse of(Order order) {
        OrdererAddress a = order.getOrdererAddress();
        Address address = new Address(
                a.getOrdererName(),
                a.getOrdererEmail(),
                a.getZipCode(),
                a.getAddressLine1(),
                a.getAddressLine2(),
                a.getCity(),
                a.getState()
        );

        List<Item> items = order.getOrderItems().stream()
                .map(oi -> new Item(
                        oi.getProductId(),
                        oi.getProductName(),
                        oi.getProductPrice(),
                        oi.getQuantity(),
                        oi.getTotalPrice()
                ))
                .toList();

        return new CreateOrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                address,
                items
        );
    }
}