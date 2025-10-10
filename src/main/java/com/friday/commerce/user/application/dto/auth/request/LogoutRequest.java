package com.friday.commerce.user.application.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @Schema(description = "리프레시 토큰", example = "eyJ0eXAiOiJKV1QiL...")
        @NotBlank(message = "리프레시 토큰: 토큰은 필수입니다.")
        String rt
) {

}
