package com.friday.commerce.payment.domain.exception;


import com.friday.commerce.core.web.exception.AppException;
import lombok.Getter;

@Getter
public class PaymentException extends AppException {

    public PaymentException(PaymentErrorCode code) {
        super(code);
    }
}
