package com.friday.commerce.user.application.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterAddressRequest(

        @NotBlank(message = "우편번호: 필수입니다.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호: 5자리 숫자여야 합니다.")
        String zipCode,

        @NotBlank(message = "주소1: 필수입니다.")
        @Size(max = 100, message = "주소1: 최대 100자까지 가능합니다.")
        String addressLine1,

        @Size(max = 100, message = "주소2: 최대 100자까지 가능합니다.")
        String addressLine2,

        @NotBlank(message = "도시: 필수입니다.")
        @Size(max = 50, message = "도시: 최대 50자까지 가능합니다.")
        String city,

        @NotBlank(message = "시/도: 필수입니다.")
        @Size(max = 50, message = "시/도: 최대 50자까지 가능합니다.")
        String state
) {

}
