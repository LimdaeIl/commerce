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
    public String issueAccessToken(Long userId, UserRole role) {
        return jwtProvider.generateAccessToken(userId, role.name());
    }

    @Override
    public String issueRefreshToken(Long userId) {
        return jwtProvider.generateRefreshToken(userId);
    }
}
