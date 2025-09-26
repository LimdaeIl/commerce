package com.friday.commerce.user.infrastructure.redis;

import com.friday.commerce.user.domain.port.UserCacheRepository;
import java.time.Duration;
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
}
