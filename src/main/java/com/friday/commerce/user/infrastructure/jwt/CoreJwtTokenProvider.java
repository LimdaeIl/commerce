package com.friday.commerce.user.infrastructure.jwt;

import com.friday.commerce.core.security.jwt.JwtProvider;
import com.friday.commerce.user.domain.entity.UserRole;
import com.friday.commerce.user.domain.port.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreJwtTokenProvider implements TokenProvider {

    private final JwtProvider jwtProvider;

    @Override
    public String issueAt(Long userId, UserRole role) {
        return jwtProvider.generateAccessToken(userId, role.name());
    }

    @Override
    public String issueRt(Long userId) {
        return jwtProvider.generateRefreshToken(userId);
    }

    @Override
    public String getJti(String token) {
        return jwtProvider.getJti(token);
    }

    @Override
    public long getTtlMs(String token) {
        return jwtProvider.getTtlMs(token);
    }
}
