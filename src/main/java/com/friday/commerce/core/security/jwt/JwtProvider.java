package com.friday.commerce.core.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
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

    public String getRtJti(String token) {
        Claims claims = parseRtClaims(token);
        return claims.getId();
    }

    public long getRtTtlMs(String rt) {
        Claims c = parseRtClaims(rt);
        return Math.max(c.getExpiration().getTime() - System.currentTimeMillis(), 0);
    }

    public long getAtTtlMs(String at) {
        Claims c = parseAtClaims(at);
        return Math.max(c.getExpiration().getTime() - System.currentTimeMillis(), 0);
    }

    public Long getRtUserId(String rt) {
        Claims c = parseRtClaims(rt);
        return Long.parseLong(c.getSubject());
    }

    public Long getAtUserId(String at) {
        Claims c = parseAtClaims(at);
        return Long.parseLong(c.getSubject());
    }






    private Claims parseRtClaims(String rt) {
        return getClaims(rt, refreshTokenKey);
    }

    private Claims parseAtClaims(String at) {
        return getClaims(at, accessTokenKey);
    }

    private Claims getClaims(String token, SecretKey key) {
        String stripped = stripBearer(token);

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .clockSkewSeconds(DEFAULT_CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(stripped)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw new TokenException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (PrematureJwtException e) {
            throw new TokenException(JwtErrorCode.PREMATURE_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new TokenException(JwtErrorCode.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {                 // 구조/인코딩 손상
            throw new TokenException(JwtErrorCode.MALFORMED_TOKEN);
        } catch (SecurityException | SignatureException e) {// 서명 위조/키 불일치
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        } catch (MissingClaimException |    // 필수 클레임 누락
                 IncorrectClaimException e) {
            throw new TokenException(JwtErrorCode.INVALID_CLAIMS);
        } catch (JwtException e) {
            log.debug("JWT parse error: {}", e.toString());
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }
    }

    private String stripBearer(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenException(JwtErrorCode.NOT_FOUND_TOKEN); // null/blank는 '없음'으로 분류
        }

        String t = token.trim();
        // "Bearer " 프리픽스가 있으면 제거(대소문자 무시)
        if (t.regionMatches(true, 0, PREFIX_BEARER, 0, PREFIX_BEARER.length())) {
            t = t.substring(PREFIX_BEARER.length()).trim();
            if (t.isEmpty()) {
                throw new TokenException(JwtErrorCode.NOT_FOUND_TOKEN);
            }
        }
        return t;
    }
}
