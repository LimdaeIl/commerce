package com.friday.commerce.catalog.presentation;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.request.UpdateCategoryNameRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.DeleteCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.GetAllCategoriesResponse;
import com.friday.commerce.catalog.application.dto.category.response.UpdateCategoryNameResponse;
import com.friday.commerce.catalog.application.usecase.CategoryUseCase;
import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "Category-V1", description = "상품 카테고리 관리 API")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/categories", produces = "application/json")
@RestController
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @Operation(
            summary = "카테고리 생성",
            description = "새 카테고리를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateCategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "검증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PostMapping(consumes = "application/json")
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info,
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        CreateCategoryResponse response = categoryUseCase.createCategory(info, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "카테고리 전체 조회",
            description = "모든 카테고리를 조회합니다. `format=tree`(기본) 또는 `format=flat`을 지원하며, `maxDepth`로 트리 깊이를 제한할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetAllCategoriesResponse.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<GetAllCategoriesResponse> getAllCategories(
            @Parameter(description = "응답 포맷", example = "tree",
                    schema = @Schema(allowableValues = {"tree", "flat"}, defaultValue = "tree"))
            @RequestParam(defaultValue = "tree") String format,

            @Parameter(description = "최대 트리 깊이(선택)", example = "5")
            @RequestParam(required = false) Integer maxDepth
    ) {
        GetAllCategoriesResponse response = categoryUseCase.getAllCategories(format, maxDepth);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "카테고리명 수정",
            description = "카테고리의 이름을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UpdateCategoryNameResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PatchMapping(value = "/{categoryId}/name", consumes = "application/json")
    public ResponseEntity<UpdateCategoryNameResponse> updateCategoryName(
            @Parameter(description = "카테고리 ID", example = "1001...")
            @PathVariable Long categoryId,

            @Valid @RequestBody UpdateCategoryNameRequest request,

            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        UpdateCategoryNameResponse response = categoryUseCase.updateName(categoryId, request, info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "카테고리 삭제",
            description = "카테고리를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = DeleteCategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<DeleteCategoryResponse> deleteCategory(
            @Parameter(description = "카테고리 ID", example = "1001...")
            @PathVariable Long categoryId,

            @Parameter(hidden = true) @CurrentUser CurrentUserInfo info
    ) {
        DeleteCategoryResponse response = categoryUseCase.deleteCategory(categoryId, info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

