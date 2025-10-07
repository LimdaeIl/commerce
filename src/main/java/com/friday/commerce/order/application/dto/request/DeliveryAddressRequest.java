package com.friday.commerce.order.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeliveryAddressRequest(

        @NotBlank(message = "받는사람이름: 받는사람이름은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]{1,12}$",
                message = "받는사람이름: 1~12자, 영문/숫자/한글/특수기호만 허용합니다."
        )
        String recipientName,


        @NotBlank(message = "이메일: 이메일은 필수입니다.")
        @Email(message = "이메일: 유효하지 않은 이메일 형식입니다.")
        String ordererEmail,

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
