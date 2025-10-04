package com.friday.commerce.catalog.application.usecase;

import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.response.CreateProductResponse;
import com.friday.commerce.core.security.model.CurrentUserInfo;

public interface ProductUseCase {

    CreateProductResponse createProduct(CreateProductRequest request, CurrentUserInfo info);
}
