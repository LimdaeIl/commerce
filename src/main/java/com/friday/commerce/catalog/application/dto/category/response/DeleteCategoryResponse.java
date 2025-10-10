package com.friday.commerce.catalog.application.dto.category.response;

import com.friday.commerce.catalog.domain.entity.Category;

public record DeleteCategoryResponse(
        Long categoryId,
        Long parentId,
        String path,
        Integer depth,
        boolean deleted
) {

    public static DeleteCategoryResponse of(Category category) {
        Long pid = (category.getParent() == null) ? null : category.getParent().getCategoryId();
        return new DeleteCategoryResponse(
                category.getCategoryId(), pid,
                category.getPath(),
                category.getDepth(), true
        );
    }
}
