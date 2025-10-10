package com.friday.commerce.user.application.dto.auth.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SignUpRequest(

        @Schema(description = "이메일", example = "alice@example.com")
        @NotBlank(message = "이메일: 이메일은 필수입니다.")
        @Email(message = "이메일: 유효하지 않은 이메일 형식입니다.")
        String email,

        @Schema(description = "비밀번호", example = "Aa!23456")
        @NotBlank(message = "비밀번호: 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]).{8,}$",
                message = "비밀번호: 최소 8자, 영문자/숫자/특수문자를 각각 1개 이상 포함해야 합니다."
        )
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,


        @Schema(description = "사용자명", example = "앨리스123")
        @NotBlank(message = "사용자명: 사용자명은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]{1,12}$",
                message = "사용자명: 1~12자, 영문/숫자/한글/특수기호만 허용합니다."
        )
        String username,

        @Schema(implementation = Agreement.class)
        @NotNull(message = "동의 정보: agreement는 필수입니다.")
        @Valid
        Agreement agreement,

        @Schema(implementation = Address.class)
        @NotNull(message = "주소 정보: address는 필수입니다.")
        @Valid
        Address address
) {

    @Schema(name = "SignUpAgreement", description = "회원가입 동의 항목")
    public record Agreement(
            @Schema(description = "이용약관", example = "true")
            @NotNull(message = "이용약관 동의: 이용약관 동의는 필수입니다.")
            Boolean termsOfService,

            @Schema(description = "개인정보 처리방침 동의", example = "true")
            @NotNull(message = "개인정보 처리방침 동의: 개인정보 처리방침 동의는 필수입니다.")
            Boolean privacy,

            @Schema(description = "마케팅 수신 동의", example = "true")
            @NotNull(message = "마케팅 수신 동의: 마케팅 수신 동의 값은 true/false 중 하나여야 합니다.")
            Boolean marketing
    ) {

    }

    @Schema(name = "SignUpAddress", description = "회원가입 기본 주소")
    public record Address(

            @Schema(description = "우편번호", example = "06236")
            @NotBlank(message = "우편번호: 필수입니다.")
            @Pattern(regexp = "^\\d{5}$", message = "우편번호: 5자리 숫자여야 합니다.")
            String zipCode,

            @Schema(description = "주소1", example = "서울시 강남구 테헤란로 123")
            @NotBlank(message = "주소1: 필수입니다.")
            @Size(max = 100, message = "주소1: 최대 100자까지 가능합니다.")
            String addressLine1,

            @Schema(description = "주소2", example = "OO빌딩 5층")
            @Size(max = 100, message = "주소2: 최대 100자까지 가능합니다.")
            String addressLine2,

            @Schema(description = "도시", example = "서울")
            @NotBlank(message = "도시: 필수입니다.")
            @Size(max = 50, message = "도시: 최대 50자까지 가능합니다.")
            String city,

            @Schema(description = "시/도", example = "강남구")
            @NotBlank(message = "시/도: 필수입니다.")
            @Size(max = 50, message = "시/도: 최대 50자까지 가능합니다.")
            String state
    ) {

    }

    // 퍼사드 메서드(서비스는 이것만 사용)
    public boolean agreedTos() {
        return Boolean.TRUE.equals(agreement.termsOfService());
    }

    public boolean agreedPrivacy() {
        return Boolean.TRUE.equals(agreement.privacy());
    }

    public boolean agreedMarketing() {
        return Boolean.TRUE.equals(agreement.marketing());
    }

    public String zipCode() {
        return address.zipCode();
    }

    public String addressLine1() {
        return address.addressLine1();
    }

    public String addressLine2() {
        return address.addressLine2();
    }

    public String city() {
        return address.city();
    }

    public String state() {
        return address.state();
    }
}
