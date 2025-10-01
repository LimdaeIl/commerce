package com.friday.commerce.catalog.application.usecase;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.core.security.model.CurrentUserInfo;

public interface CategoryUseCase {

    CreateCategoryResponse createCategory(CurrentUserInfo info, CreateCategoryRequest request);
}
