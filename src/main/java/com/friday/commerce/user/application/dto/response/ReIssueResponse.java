package com.friday.commerce.user.application.dto.response;

import com.friday.commerce.user.domain.entity.User;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ReIssueResponse(
        Long userId,
        String newAt,
        String newRt,
        long newAtTtlMs,
        long newRtTtlMs
) {

    public static ReIssueResponse of(User userById, String newAt, String newRt, long newAtTtlMs,
            long newRtTtlMs) {
        return ReIssueResponse.builder()
                .userId(userById.getUserId())
                .newAt(newAt)
                .newRt(newRt)
                .newAtTtlMs(newAtTtlMs)
                .newRtTtlMs(newRtTtlMs)
                .build();
    }
}
