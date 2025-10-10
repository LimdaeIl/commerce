package com.friday.commerce.user.infrastructure.email;

import com.friday.commerce.user.domain.port.EmailVerificationRepositoryPort;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EmailVerificationRepository implements EmailVerificationRepositoryPort {

    private final StringRedisTemplate template;

    private static final String PREFIX = "email";

    private static String kCode(String email) {
        return PREFIX + ":CODE:" + email;
    }

    private static String kAttempts(String email) {
        return PREFIX + ":ATTEMPTS:" + email;
    }

    private static String kCool(String email) {
        return PREFIX + ":COOL:" + email;
    }

    private static String kBlock(String email) {
        return PREFIX + ":BLOCK:" + email;
    }

    private static String kVerified(String email) {
        return PREFIX + ":VERIFIED:" + email;
    }

    @Override
    public void saveCode(String email, String code, Duration ttl) {
        template.opsForValue().set(kCode(email), code, ttl);
    }

    @Override
    public Optional<String> getCode(String email) {
        return Optional.ofNullable(template.opsForValue().get(kCode(email)));
    }

    @Override
    public void deleteCode(String email) {
        template.delete(kCode(email));
    }

    @Override
    public long incrementAttempts(String email, Duration windowTtl) {
        var ops = template.boundValueOps(kAttempts(email));
        Long v = ops.increment();
        if (v != null && v == 1L) {
            ops.expire(windowTtl); // 첫 증가 시 윈도우 TTL 부여
        }
        return v == null ? 0L : v;
    }

    @Override
    public void resetAttempts(String email) {
        template.delete(kAttempts(email));
    }

    @Override
    public void setCooltime(String email, Duration coolTtl) {
        // 중복 전송 레이스 방지
        template.opsForValue().setIfAbsent(kCool(email), "1", coolTtl);
    }

    @Override
    public boolean inCooltime(String email) {
        return Boolean.TRUE.equals(template.hasKey(kCool(email))); // null-safe
    }

    @Override
    public void block(String email, Duration ttl) {
        // 이미 차단이면 TTL 덮어쓰지 않음
        template.opsForValue().setIfAbsent(kBlock(email), "1", ttl);
    }

    @Override
    public boolean isBlocked(String email) {
        return Boolean.TRUE.equals(template.hasKey(kBlock(email)));
    }

    @Override
    public void markVerified(String email, Duration ttl) {
        template.opsForValue().set(kVerified(email), "1", ttl);
    }

    @Override
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(template.hasKey(kVerified(email)));
    }

    @Override
    public void clearVerified(String email) {
        template.delete(kVerified(email));
    }
}
