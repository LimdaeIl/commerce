package com.friday.commerce.user.domain.port;

import com.friday.commerce.core.security.model.UserRole;

public interface TokenProvider {

    String issueAt(Long userId, UserRole role);

    String issueRt(Long userId);

    String getRtJti(String token);

    String getAtJti(String token);

    long getRtTtlMs(String rt);

    long getAtTtlMs(String at);

    Long getRtUserId(String rt);

    Long getAtUserId(String at);
}
