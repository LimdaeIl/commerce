package com.friday.commerce.order.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdererAddress {

    private String ordererName;
    private String ordererEmail;
    private String zipCode;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;

    private OrdererAddress(
            String ordererName,
            String ordererEmail,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state
    ) {
        this.ordererName = ordererName;
        this.ordererEmail = ordererEmail;
        this.zipCode = zipCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
    }

    public static OrdererAddress create(
            String ordererName,
            String ordererEmail,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state
    ) {
        return new OrdererAddress(
                ordererName,
                ordererEmail,
                zipCode,
                addressLine1,
                addressLine2,
                city,
                state
        );
    }
}
