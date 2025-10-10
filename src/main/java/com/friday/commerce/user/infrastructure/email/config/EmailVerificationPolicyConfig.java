package com.friday.commerce.user.infrastructure.email.config;

import com.friday.commerce.user.domain.policy.EmailVerificationPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailVerificationProperties.class)
public class EmailVerificationPolicyConfig {

    // @ConfigurationProperties는 infra에 두고,
    // 매핑은 infra에서 수행하여 EmailVerificationPolicy 빈을 생성
    // 이렇게 하면 Domain은 오직 JDK 타입만 알고, Spring/설정/infra에 전혀 의존하지 않습니다.
    // 더 느슨하게 하려면 application에 포트를 두고, infra에서 구현하면 됩니다...
    @Bean
    public EmailVerificationPolicy emailVerificationPolicy(EmailVerificationProperties p) {
        return new EmailVerificationPolicy(
                p.codeValidityDuration(),
                p.resendCooldownDuration(),
                p.maxAttempts(),
                p.lockoutDuration(),
                p.successValidityDuration()
        );
    }
}
