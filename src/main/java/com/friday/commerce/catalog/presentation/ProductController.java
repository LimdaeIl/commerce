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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product-V1", description = "상품 관리 API")
@SecurityRequirement(name = "BearerAuth") // 이 컨트롤러의 API는 인증 필요
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/products", produces = "application/json")
@RestController
public class ProductController {

    private final ProductUseCase productUseCase;

    @Operation(
            summary = "상품 생성",
            description = "새 상품을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PostMapping(consumes = "application/json")
    public ResponseEntity<GetProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.createProduct(request, info);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "상품 단건 조회",
            description = "상품 ID로 상품을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<GetProductResponse> getProduct(
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId
    ) {
        GetProductResponse response = productUseCase.getProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "상품 목록 조회",
            description = "상품명을 부분 일치로 검색하고, 카테고리/가격 범위/페이지네이션으로 필터링합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class,
                            description = "content: GetAllProductsResponse[]를 포함하는 페이지 응답")))
    })
    @GetMapping
    public ResponseEntity<PageResponse<GetAllProductsResponse>> getAllProducts(
            @Parameter(description = "상품명(부분 일치 검색)", example = "이어폰")
            @RequestParam(required = false) String productName,

            @Parameter(description = "카테고리 ID", example = "2001")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "최소 가격(원)", example = "10000")
            @RequestParam(required = false) Integer minPrice,

            @Parameter(description = "최대 가격(원)", example = "300000")
            @RequestParam(required = false) Integer maxPrice,

            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Direction.DESC)
            }) Pageable pageable
    ) {
        PageResponse<GetAllProductsResponse> response = productUseCase.getAllProducts(
                productName, categoryId, minPrice, maxPrice, pageable
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "재고 증가",
            description = "해당 상품의 재고를 증가시킵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "증가 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping(value = "/{productId}/increase", consumes = "application/json")
    public ResponseEntity<GetProductResponse> increaseStock(
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId,
            @Valid @RequestBody IncreaseStockRequest request
    ) {
        GetProductResponse response = productUseCase.increaseStock(productId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "재고 감소",
            description = "해당 상품의 재고를 감소시킵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "감소 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping(value = "/{productId}/decrease", consumes = "application/json")
    public ResponseEntity<GetProductResponse> decreaseStock(
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId,
            @Valid @RequestBody DecreaseStockRequest request
    ) {
        GetProductResponse response = productUseCase.decreaseStock(productId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "상품 삭제",
            description = "상품을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @DeleteMapping("/{productId}/delete")
    public ResponseEntity<Void> decreaseStock( // 메서드명은 그대로 둠
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        productUseCase.delete(productId, info);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "상품 상태: 미공개",
            description = "상품 상태를 DRAFT(미공개)로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/status/draft")
    public ResponseEntity<GetProductResponse> statusDraft(
            @Parameter(description = "상품 ID", example = "10001....")
            @PathVariable Long productId,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.statusDraft(productId, info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "상품 상태: 공개",
            description = "상품 상태를 PUBLISHED(공개)로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/published")
    public ResponseEntity<GetProductResponse> statusPublished(
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.statusPublished(productId, info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "상품 상태: 보관됨",
            description = "상품 상태를 ARCHIVED(보관)로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping("/{productId}/archived")
    public ResponseEntity<GetProductResponse> statusArchived(
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        GetProductResponse response = productUseCase.statusArchived(productId, info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "상품 수정",
            description = "상품 기본 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = GetProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping(value = "/{productId}", consumes = "application/json")
    public ResponseEntity<GetProductResponse> updateProduct(
            @Parameter(description = "상품 ID", example = "10001...")
            @PathVariable Long productId,
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        GetProductResponse response = productUseCase.updateProduct(productId, info, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
