package com.friday.commerce.user.application.dto.auth.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record SendCodeEmailResponse(
        String recipient,
        long validMinutes
) {

    public static SendCodeEmailResponse of(String email, long minutes) {

        return SendCodeEmailResponse.builder()
                .recipient(email)
                .validMinutes(minutes)
                .build();
    }
}
