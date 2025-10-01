package com.friday.commerce.catalog.presentation;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.catalog.application.usecase.CategoryUseCase;
import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
            @RequestBody CreateCategoryRequest request
    ) {
        CreateCategoryResponse response = categoryUseCase.createCategory(info, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
