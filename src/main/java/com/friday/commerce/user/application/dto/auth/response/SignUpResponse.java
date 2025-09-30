package com.friday.commerce.user.application.dto.auth.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.user.domain.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(access = AccessLevel.PRIVATE)
public record SignUpResponse(
        Long userId,
        String email,
        String username,
        UserRole userRole,
        LocalDateTime createdAt,
        boolean isLocked,
        Agreement userAgreement,
        List<Address> userAddresses
) {

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userRole(user.toCoreRole())
                .createdAt(user.getCreatedAt())
                .isLocked(Boolean.TRUE.equals(user.getIsLocked()))
                .userAgreement(Agreement.from(user))
                .userAddresses(Address.from(user))
                .build();
    }

    // 외부 노출 막기: private 중첩 record + 필드 직렬화
    @JsonAutoDetect(fieldVisibility = ANY)
    private record Agreement(
            boolean termsOfService,
            boolean privacy,
            boolean marketing,
            LocalDateTime agreedAt
    ) {
        static Agreement from(User user) {
            var ua = user.getUserAgreement();
            return new Agreement(
                    ua.isTermsOfService(),
                    ua.isPrivacy(),
                    ua.isMarketing(),
                    ua.getAgreedAt()
            );
        }
    }

    // 외부 노출 막기: private 중첩 record + 필드 직렬화
    @JsonAutoDetect(fieldVisibility = ANY)
    private record Address(
            Long addressId,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            boolean isDefault
    ) {
        static List<Address> from(User user) {
            return user.getUserAddresses().stream()
                    .map(addr -> new Address(
                            addr.getAddressId(),
                            addr.getZipCode(),
                            addr.getAddressLine1(),
                            addr.getAddressLine2(),
                            addr.getCity(),
                            addr.getState(),
                            Boolean.TRUE.equals(addr.getIsDefault())
                    ))
                    .collect(Collectors.toList());
        }
    }
}
