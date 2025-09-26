package com.friday.commerce.user.application.dto.response;

import com.friday.commerce.user.domain.entity.User;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SignInResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {

    public static SignInResponse from(User user, String accessToken, String refreshToken) {
        return SignInResponse.builder()
                .userId(user.getUserId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
