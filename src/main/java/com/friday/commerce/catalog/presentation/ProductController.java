package com.friday.commerce.catalog.presentation;

import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.request.DecreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.request.IncreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.request.UpdateProductRequest;
import com.friday.commerce.catalog.application.dto.product.response.GetAllProductsResponse;
import com.friday.commerce.catalog.application.dto.product.response.GetProductResponse;
import com.friday.commerce.catalog.application.usecase.ProductUseCase;
import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.core.web.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductController {

    private final ProductUseCase productUseCase;

    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PostMapping
    public ResponseEntity<GetProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.createProduct(request, info);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<GetProductResponse> getProduct(
            @PathVariable Long productId
    ) {
        GetProductResponse response = productUseCase.getProduct(productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }

    @GetMapping
    public ResponseEntity<PageResponse<GetAllProductsResponse>> getAllProducts(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Direction.DESC)
            }) Pageable pageable
    ) {
        PageResponse<GetAllProductsResponse> response = productUseCase.getAllProducts(
                productName,
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 재고 증가
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/increase")
    public ResponseEntity<GetProductResponse> increaseStock(
            @PathVariable Long productId,
            @RequestBody @Valid IncreaseStockRequest request
    ) {
        GetProductResponse response = productUseCase.increaseStock(productId, request);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 재고 감소
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/decrease")
    public ResponseEntity<GetProductResponse> decreaseStock(
            @PathVariable Long productId,
            @RequestBody @Valid DecreaseStockRequest request
    ) {
        GetProductResponse response = productUseCase.decreaseStock(productId, request);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 상품 삭제
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<Void> decreaseStock(
            @PathVariable Long productId,
            @CurrentUser CurrentUserInfo info
    ) {
        productUseCase.delete(productId, info);

        return ResponseEntity
                .noContent()
                .build();
    }

    // 상품 상태 미공개
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/status/draft")
    public ResponseEntity<GetProductResponse> statusDraft(
            @PathVariable Long productId,
            @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.statusDraft(productId, info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    // 상품 상태 공개
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/published")
    public ResponseEntity<GetProductResponse> statusPublished(
            @PathVariable Long productId,
            @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.statusPublished(productId, info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 상품 상태 보관됨
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/archived")
    public ResponseEntity<GetProductResponse> statusArchived(
            @PathVariable Long productId,
            @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.statusArchived(productId, info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 상품 수정
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}")
    public ResponseEntity<GetProductResponse> updateProduct(
            @PathVariable Long productId,
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        GetProductResponse response = productUseCase.updateProduct(productId, info, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
