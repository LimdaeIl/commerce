package com.friday.commerce.user.application.dto.auth.response;

import com.friday.commerce.user.domain.entity.User;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SignInResponse(
        Long userId,
        String at,
        String rt,
        long atTtlMs,
        long rtTtlMs
) {

    public static SignInResponse of(User user, String at, String rt, long atTtlMs, long rtTtlMs) {
        return SignInResponse.builder()
                .userId(user.getUserId())
                .at(at)
                .rt(rt)
                .atTtlMs(atTtlMs)
                .rtTtlMs(rtTtlMs)
                .build();
    }
}
