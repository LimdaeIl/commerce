package com.friday.commerce.user.application.dto.response;

import com.friday.commerce.user.domain.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import com.friday.commerce.core.security.model.UserRole;

@Builder(access = AccessLevel.PRIVATE)
public record SignUpResponse(
        Long userId,
        String email,
        String username,
        UserRole userRole,
        LocalDateTime createdAt,
        boolean isLocked,
        UserAgreement userAgreement,
        List<UserAddress> userAddresses
) {

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userRole(user.toCoreRole()) // 내부 enum → core enum 변환
                .createdAt(user.getCreatedAt())
                .isLocked(Boolean.TRUE.equals(user.getIsLocked()))
                .userAgreement(UserAgreement.from(user))
                .userAddresses(UserAddress.from(user))
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record UserAgreement(
            boolean termsOfService,
            boolean privacy,
            boolean marketing,
            LocalDateTime agreedAt
    ) {
        public static UserAgreement from(User user) {
            var ua = user.getUserAgreement();
            return new UserAgreement(
                    ua.isTermsOfService(),
                    ua.isPrivacy(),
                    ua.isMarketing(),
                    ua.getAgreedAt()
            );
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record UserAddress(
            Long addressId,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            boolean isDefault
    ) {
        public static List<UserAddress> from(User user) {
            return user.getUserAddresses().stream()
                    .map(addr -> new UserAddress(
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
