package com.friday.commerce.catalog.application.dto.category.request;

public record CreateCategoryRequest(
        String path,

        String delimiter, // 구분 기호: 슬래시 /

        Long startParentId, // 시작 부모 ID. 없으면 null

        Integer maxDepthOverride
) {
    public String delimiterOrDefault() {
        return (delimiter == null || delimiter.isBlank()) ? "/" : delimiter;
    }
}
