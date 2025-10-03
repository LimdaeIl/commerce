package com.friday.commerce.catalog.application.dto.category.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateCategoryNameResponse(
        Long categoryId,
        String name,
        Long parentId,
        String path,
        Integer depth
) {}