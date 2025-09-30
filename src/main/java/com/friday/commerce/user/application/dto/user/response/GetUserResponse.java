package com.friday.commerce.user.application.dto.user.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.entity.UserAddress;
import com.friday.commerce.user.domain.entity.UserAgreement;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetUserResponse(
        Long userId,
        String email,
        String username,
        Agreement agreement,
        List<Address> addresses
) {
    public static GetUserResponse from(User u) {
        return new GetUserResponse(
                u.getUserId(),
                u.getEmail(),
                u.getUsername(),
                Agreement.from(u.getUserAgreement()),
                Address.from(u.getUserAddresses())
        );
    }

    @JsonAutoDetect(fieldVisibility = ANY) // 필드 기반 직렬화 허용
    private static record Agreement(
            Boolean termsOfService,
            Boolean privacy,
            Boolean marketing
    ) {
        static Agreement from(UserAgreement ua) {
            return new Agreement(ua.isTermsOfService(), ua.isPrivacy(), ua.isMarketing());
        }
    }

    @JsonAutoDetect(fieldVisibility = ANY) // 필드 기반 직렬화 허용
    private static record Address(
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state
    ) {
        static List<Address> from(List<UserAddress> list) {
            return list.stream()
                    .map(a -> new Address(
                            a.getZipCode(),
                            a.getAddressLine1(),
                            a.getAddressLine2(),
                            a.getCity(),
                            a.getState()))
                    .toList();
        }
    }
}
