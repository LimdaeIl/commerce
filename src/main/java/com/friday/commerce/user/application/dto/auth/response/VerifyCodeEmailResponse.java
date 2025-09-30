package com.friday.commerce.user.application.dto.auth.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record VerifyCodeEmailResponse(
        String recipient,
        long verifiedUntilEpochMillis
) {
    public static VerifyCodeEmailResponse of(String recipient, long verifiedUntilEpochMillis) {
        return VerifyCodeEmailResponse.builder()
                .recipient(recipient)
                .verifiedUntilEpochMillis(verifiedUntilEpochMillis)
                .build();
    }

}
