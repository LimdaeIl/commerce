package com.friday.commerce.order.domain.exception;

import com.friday.commerce.core.web.exception.AppException;

public class OrderException extends AppException {
    public OrderException(OrderErrorCode errorCode) { super(errorCode); }
}