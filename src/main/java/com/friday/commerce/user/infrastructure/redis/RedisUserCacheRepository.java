package com.friday.commerce.user.infrastructure.redis;

import com.friday.commerce.user.domain.port.UserCacheRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisUserCacheRepository implements UserCacheRepository {

    private final StringRedisTemplate template;

    private static final String PREFIX = "user";

    private static String kRt(String jti) {
        return PREFIX + ":RT:" + jti; // Key Refresh Token: "user:RT:{jti}"
    }

    private static String kUserRt(Long userId) {
        return PREFIX + ":USER:" + userId;
    }

    private static String kBlAt(String jti) {
        return PREFIX + ":BL:AT:" + jti;
    }

    private static String kBlRt(String jti) {
        return PREFIX + ":BL:RT:" + jti;
    }

    @Override
    public void saveToken(Long userId, String jti, long ttlMillis) {
        template.opsForValue().set(kUserRt(userId), jti, Duration.ofMillis(ttlMillis));
    }

    @Override
    public void atSetBl(String atJti, long atTtlMs) {
        if (atTtlMs <= 0) return ;
        template.opsForValue().set(kBlAt(atJti), "1", Duration.ofMillis(atTtlMs));
    }

    @Override
    public void rtSetBl(String rtJti, long rtTtlMs) {
        if (rtTtlMs <= 0) return ;
        template.opsForValue().set(kBlRt(rtJti), "1", Duration.ofMillis(rtTtlMs));
    }

    public boolean isAtBl(String atJti) {
        return template.hasKey(kBlAt(atJti));
    }

    public boolean isRtBl(String rtJti) {
        return template.hasKey(kBlRt(rtJti));
    }


    @Override
    public Optional<String> getRtJti(Long rtJti) {
        return Optional.ofNullable(template.opsForValue().get(kUserRt(rtJti)));
    }

    @Override
    public void deleteRt(Long rtUserId) {
        template.delete(kUserRt(rtUserId));
    }


}
