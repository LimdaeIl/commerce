package com.friday.commerce.user.application.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyCodeEmailRequest(
        @Schema(description = "이메일", example = "alice@example.com")
        @NotBlank(message = "이메일: 이메일은 필수입니다.")
        @Email(message = "이메일: 유효하지 않은 이메일 형식입니다.")
        String email,

        @Schema(description = "코드", example = "220565")
        @NotNull
        @Min(100000)
        @Max(999999)
        Integer code
) {

}
