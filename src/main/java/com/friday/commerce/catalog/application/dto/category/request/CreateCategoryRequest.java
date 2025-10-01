package com.friday.commerce.catalog.application.dto.category.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "경로(path): 필수입니다.")
        @Size(max = 500, message = "경로(path): 최대 500자까지 가능합니다.")
        String path,

        @Size(min = 1, max = 3, message = "구분 기호(delimiter): 1~3자여야 합니다.")
        @Pattern(regexp = "^\\S+$", message = "구분 기호(delimiter): 공백 문자를 포함할 수 없습니다.")
        String delimiter,

        @Positive(message = "시작 부모 ID(startParentId): 양수여야 합니다.")
        Long startParentId,

        @Positive(message = "maxDepthOverride: 1 이상의 정수여야 합니다.")
        @Max(value = 32, message = "maxDepthOverride: 시스템 허용 깊이를 초과했습니다(최대 7).")
        Integer maxDepthOverride
) {
    public String delimiterOrDefault() {
        return (delimiter == null || delimiter.isBlank()) ? "/" : delimiter;
    }
}
