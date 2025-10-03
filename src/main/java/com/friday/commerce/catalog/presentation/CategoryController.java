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

@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@RestController
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @RequireRole({UserRole.ADMIN, UserRole.SELLER})
    @PostMapping
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid CreateCategoryRequest request
    ) {
        CreateCategoryResponse response = categoryUseCase.createCategory(info, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<GetAllCategoriesResponse> getAllCategories(
            @RequestParam(defaultValue = "tree") String format,
            @RequestParam(required = false) Integer maxDepth

    ) {
        GetAllCategoriesResponse response = categoryUseCase.getAllCategories(format, maxDepth);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

    }


    // 카테고리명 수정
    @PatchMapping("/{categoryId}/name")
    public ResponseEntity<UpdateCategoryNameResponse> updateCategoryName(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryNameRequest request,
            @CurrentUser CurrentUserInfo info
    ) {
        UpdateCategoryNameResponse response = categoryUseCase.updateName(categoryId, request, info);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    // 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<DeleteCategoryResponse> deleteCategory(
            @PathVariable Long categoryId,
            @CurrentUser CurrentUserInfo info
    ) {
        DeleteCategoryResponse response = categoryUseCase.deleteCategory(categoryId, info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
