package com.friday.commerce.user.domain.exception;


import com.friday.commerce.core.web.exception.AppException;

public class UserException extends AppException {
    public UserException(UserErrorCode code) { super(code); }
}
