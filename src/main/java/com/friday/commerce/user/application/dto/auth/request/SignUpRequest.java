package com.friday.commerce.user.application.dto.auth.request;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "password")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY)
public class SignUpRequest {

    @NotBlank(message = "이메일: 이메일은 필수입니다.")
    @Email(message = "이메일: 유효하지 않은 이메일 형식입니다.")
    private String email;

    @NotBlank(message = "비밀번호: 비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]).{8,}$",
            message = "비밀번호: 최소 8자, 영문자/숫자/특수문자를 각각 1개 이상 포함해야 합니다."
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "사용자명: 사용자명은 필수입니다.")
    @Pattern(
            regexp = "^[A-Za-z0-9가-힣!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]{1,12}$",
            message = "사용자명: 1~12자, 영문/숫자/한글/특수기호만 허용합니다."
    )
    private String username;

    @NotNull(message = "동의 정보: agreement는 필수입니다.")
    @Valid
    private Agreement agreement;

    @NotNull(message = "주소 정보: address는 필수입니다.")
    @Valid
    private Address address;

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonAutoDetect(fieldVisibility = ANY) // ← 내부 클래스도 필드 바인딩 허용
    private static class Agreement {

        @NotNull(message = "이용약관 동의는 필수입니다.")
        private Boolean termsOfService;

        @NotNull(message = "개인정보 처리방침 동의는 필수입니다.")
        private Boolean privacy;

        @NotNull(message = "마케팅 수신 동의 값은 true/false 중 하나여야 합니다.")
        private Boolean marketing;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonAutoDetect(fieldVisibility = ANY)
    private static class Address {

        @NotBlank(message = "우편번호: 필수입니다.")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호: 5자리 숫자여야 합니다.")
        private String zipCode;

        @NotBlank(message = "주소1: 필수입니다.")
        @Size(max = 100, message = "주소1: 최대 100자까지 가능합니다.")
        private String addressLine1;

        @Size(max = 100, message = "주소2: 최대 100자까지 가능합니다.")
        private String addressLine2;

        @NotBlank(message = "도시: 필수입니다.")
        @Size(max = 50, message = "도시: 최대 50자까지 가능합니다.")
        private String city;

        @NotBlank(message = "시/도: 필수입니다.")
        @Size(max = 50, message = "시/도: 최대 50자까지 가능합니다.")
        private String state;
    }

    // 퍼사드 메서드(서비스는 이것만 사용)
    public boolean agreedTos() {
        return agreement.termsOfService;
    }

    public boolean agreedPrivacy() {
        return agreement.privacy;
    }

    public boolean agreedMarketing() {
        return agreement.marketing;
    }

    public String zipCode() {
        return address.zipCode;
    }

    public String addressLine1() {
        return address.addressLine1;
    }

    public String addressLine2() {
        return address.addressLine2;
    }

    public String city() {
        return address.city;
    }

    public String state() {
        return address.state;
    }
}
