package com.friday.commerce.catalog.application.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryNameRequest(
        @NotBlank(message = "카테고리명: 값은 필수입니다.")
        @Size(max = 50, message = "카테고리명: 최대 50자까지 가능합니다.")
        String name
) {

}
