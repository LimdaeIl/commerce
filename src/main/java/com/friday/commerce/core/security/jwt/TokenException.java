package com.friday.commerce.core.security.jwt;

import com.friday.commerce.core.web.exception.AppException;
import com.friday.commerce.core.web.exception.ErrorCode;

public class TokenException extends AppException {

    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
