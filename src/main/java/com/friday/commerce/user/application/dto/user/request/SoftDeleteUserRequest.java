package com.friday.commerce.user.application.dto.user.request;

import jakarta.validation.constraints.NotBlank;

public record SoftDeleteUserRequest(
        @NotBlank(message = "리프레시 토큰: 토큰은 필수입니다.")
        String rt
) {

}
