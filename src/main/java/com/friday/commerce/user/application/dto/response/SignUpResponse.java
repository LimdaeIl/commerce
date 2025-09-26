package com.friday.commerce.user.application.dto.response;

import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SignUpResponse(
        Long userId,
        String email,
        String username,
        UserRole userRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long updatedBy,
        LocalDateTime deletedAt,
        Long deletedBy,
        boolean isLocked,
        UserAgreement userAgreement,
        List<UserAddress> userAddresses

) {

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userRole(user.getUserRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .deletedAt(user.getDeletedAt())
                .deletedBy(user.getDeletedBy())
                .isLocked(user.getIsLocked())
                .userAgreement(UserAgreement.from(user))
                .userAddresses(UserAddress.from(user))
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    record UserAgreement(
            boolean termsOfService,
            boolean privacy,
            boolean marketing,
            LocalDateTime agreedAt
    ) {

        public static UserAgreement from(User user) {
            return UserAgreement.builder()
                    .termsOfService(user.getUserAgreement().isTermsOfService())
                    .privacy(user.getUserAgreement().isPrivacy())
                    .marketing(user.getUserAgreement().isMarketing())
                    .agreedAt(user.getUserAgreement().getAgreedAt())
                    .build();
        }
    }

    @Builder(access = AccessLevel.PRIVATE)
    record UserAddress(
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
                    .map(address -> UserAddress.builder()
                            .addressId(user.getUserAddresses().getFirst().getAddressId())
                            .zipCode(user.getUserAddresses().getFirst().getZipCode())
                            .addressLine1(user.getUserAddresses().getFirst().getAddressLine1())
                            .addressLine2(user.getUserAddresses().getFirst().getAddressLine2())
                            .city(user.getUserAddresses().getFirst().getCity())
                            .state(user.getUserAddresses().getFirst().getState())
                            .isDefault(user.getUserAddresses().getFirst().getIsDefault())
                            .build()
                    ).toList();
        }
    }
}
