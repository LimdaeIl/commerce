package com.friday.commerce.user.domain.port;

import java.util.Optional;

public interface UserCacheRepository {

    void saveToken(Long userId, String jti, long ttlMillis);

    void atBlackList(String atJti, long atTtlMs);

    void rtBlackList(String rtJti, long rtTtlMs);

    Optional<String> getRtJti(Long rtJti);

    void deleteRt(Long rtUserId);
}


