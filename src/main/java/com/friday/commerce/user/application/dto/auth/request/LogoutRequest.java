package com.friday.commerce.user.application.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "리프레시 토큰: 토큰은 필수입니다.")
        String rt
) {

}
