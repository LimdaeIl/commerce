package com.friday.commerce.catalog.application.usecase;

import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.request.DecreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.request.IncreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.response.GetAllProductsResponse;
import com.friday.commerce.catalog.application.dto.product.response.GetProductResponse;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.web.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ProductUseCase {

    GetProductResponse createProduct(CreateProductRequest request, CurrentUserInfo info);

    PageResponse<GetAllProductsResponse> getAllProducts(
            String productName,
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    );

    GetProductResponse increaseStock(Long productId, IncreaseStockRequest request);
    GetProductResponse decreaseStock(Long productId, DecreaseStockRequest request);

    GetProductResponse delete(Long productId, CurrentUserInfo info);

    GetProductResponse getProduct(Long productId);

    GetProductResponse statusDraft(Long productId, CurrentUserInfo info);

    GetProductResponse statusPublished(Long productId, CurrentUserInfo info);

    GetProductResponse statusArchived(Long productId, CurrentUserInfo info);
}
