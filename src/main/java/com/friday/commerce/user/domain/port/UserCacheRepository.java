package com.friday.commerce.user.domain.port;

import java.util.Optional;

public interface UserCacheRepository {

    void saveRefreshToken(Long userId, String jti, String refreshToken, long ttlMillis);

    Optional<String> getRefreshToken(String jti);
}


