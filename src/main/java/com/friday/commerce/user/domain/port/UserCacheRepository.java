package com.friday.commerce.user.domain.port;

public interface UserCacheRepository {

    void saveToken(Long userId, String jti, long ttlMillis);
}


