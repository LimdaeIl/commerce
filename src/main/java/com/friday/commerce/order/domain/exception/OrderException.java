package com.friday.commerce.order.domain.exception;

public class OrderException extends RuntimeException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode.getMessage());
    }
}
