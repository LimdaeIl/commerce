package com.friday.commerce.user.domain.policy;

import java.time.Duration;

public record EmailVerificationPolicy(
        Duration codeValidityDuration,
        Duration resendCooldownDuration,
        int maxAttemptCount,
        Duration lockoutDuration,
        Duration successValidityDuration
) {

}
