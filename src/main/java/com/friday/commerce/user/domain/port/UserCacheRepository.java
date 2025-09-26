package com.friday.commerce.user.domain.port;

import java.util.Optional;

public interface UserCacheRepository {

    void saveToken(Long userId, String jti, long ttlMillis);

    Optional<String> getToken(String jti);
}


