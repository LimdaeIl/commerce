package com.friday.commerce.catalog.application.dto.category.response;


public record CategoryFlatRow(
        Long categoryId,
        String name,
        Long parentId,
        String path,
        Integer depth
) {}
