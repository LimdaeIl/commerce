package com.friday.commerce.catalog.domain.exception;

import com.friday.commerce.core.web.exception.AppException;

public class ProductException extends AppException {
    public ProductException(ProductErrorCode code) { super(code); }
}
