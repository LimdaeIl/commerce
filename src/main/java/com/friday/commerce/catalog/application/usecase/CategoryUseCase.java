package com.friday.commerce.catalog.application.usecase;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.request.UpdateCategoryNameRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.DeleteCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.GetAllCategoriesResponse;
import com.friday.commerce.catalog.application.dto.category.response.UpdateCategoryNameResponse;
import com.friday.commerce.core.security.model.CurrentUserInfo;

public interface CategoryUseCase {

    CreateCategoryResponse createCategory(CurrentUserInfo info, CreateCategoryRequest request);

    GetAllCategoriesResponse getAllCategories(String format, Integer maxDepth);

    UpdateCategoryNameResponse updateName(Long categoryId,  UpdateCategoryNameRequest request, CurrentUserInfo info);

    DeleteCategoryResponse deleteCategory(Long categoryId, CurrentUserInfo info);
}
