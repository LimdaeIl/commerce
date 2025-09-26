package com.friday.commerce.user.domain.port;

import com.friday.commerce.user.domain.entity.UserRole;

public interface TokenProvider {
    String issueAccessToken(Long userId, UserRole role);
    String issueRefreshToken(Long userId);
}