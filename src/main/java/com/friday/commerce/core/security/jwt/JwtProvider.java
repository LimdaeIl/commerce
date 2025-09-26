package com.friday.commerce.core.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j(topic = "JwtProvider")
@Component
public class JwtProvider {

    private static final String PREFIX_BEARER = "Bearer ";
    private static final String CLAIM_USER_ROLE = "USER_ROLE";
    private static final long DEFAULT_CLOCK_SKEW_SECONDS = 120; // 2분 오차 허용

    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    public JwtProvider(
            @Value("${spring.jwt.secret-access}") String accessSecretBase64,
            @Value("${spring.jwt.secret-refresh}") String refreshSecretBase64,
            @Value("${spring.jwt.access-token-expiration}") long accessTokenExpirationTime,
            @Value("${spring.jwt.refresh-token-expiration}") long refreshTokenExpirationTime
    ) {
        this.accessTokenKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecretBase64));
        this.refreshTokenKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretBase64));
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public String generateAccessToken(Long userId, String userRole) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpirationTime);

        return Jwts.builder()
                .header().type("JWT")
                .and()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .claim(CLAIM_USER_ROLE, userRole)
                .id(UUID.randomUUID().toString())
                .signWith(accessTokenKey, SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .header().type("JWT")
                .and()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .signWith(refreshTokenKey, SIG.HS256)
                .compact();
    }
}
