package com.friday.commerce.user.application.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordRequest(
        @NotBlank(message = "비밀번호: 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]).{8,}$",
                message = "비밀번호: 최소 8자, 영문자/숫자/특수문자를 각각 1개 이상 포함해야 합니다."
        )
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        @NotBlank(message = "비밀번호: 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]).{8,}$",
                message = "비밀번호: 최소 8자, 영문자/숫자/특수문자를 각각 1개 이상 포함해야 합니다."
        )
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String newPassword,

        String rt
) {

}
