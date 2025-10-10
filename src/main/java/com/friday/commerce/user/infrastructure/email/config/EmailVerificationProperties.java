package com.friday.commerce.user.infrastructure.email.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.email")
public record EmailVerificationProperties(
        Duration codeValidityDuration,
        Duration resendCooldownDuration,
        int maxAttempts,
        Duration lockoutDuration,
        Duration successValidityDuration
) {

}

