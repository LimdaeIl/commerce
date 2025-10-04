package com.friday.commerce.catalog.application.usecase;

import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.response.CreateProductResponse;
import com.friday.commerce.catalog.application.dto.product.response.GetAllProductsResponse;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.web.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ProductUseCase {

    CreateProductResponse createProduct(CreateProductRequest request, CurrentUserInfo info);

    PageResponse<GetAllProductsResponse> getAllProducts(
            String productName,
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    );
}
