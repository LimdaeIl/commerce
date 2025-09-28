package com.friday.commerce.core.web.filter;

import com.friday.commerce.core.security.jwt.JwtProvider;
import com.friday.commerce.core.security.jwt.TokenException;
import com.friday.commerce.core.utils.AuthKeys;
import com.friday.commerce.core.web.exception.AppErrorCode;
import com.friday.commerce.core.web.exception.AppException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * JWT 인증 필터 (Spring Security 미사용)
 *
 * <p>경로 정책에 따라 JWT를 처리한다:
 * - EXCLUDE  : 완전 공개. 토큰이 있어도/깨져도 인증을 시도하지 않고 즉시 통과 - OPTIONAL : 토큰이 있으면 검증 후 request
 * attribute(userId/role) 세팅, 없거나 깨져도 통과 - REQUIRED : 토큰 필수. 없거나 유효하지 않으면 AppException(401) 발생
 *
 * <p>검증 성공 시 아래 request attribute를 세팅한다:
 * - {@code AuthKeys.Attr.USER_ID}  : Long - {@code AuthKeys.Attr.USER_ROLE}: String(또는 enum을 쓴다면 해당
 * 타입)
 *
 * <p>주의:
 * - 이 필터는 AppException만 던지고, JSON 응답은 상위 {@code ExceptionHandlingFilter}가 표준 포맷으로 변환한다. - 절대 토큰
 * 원문/민감 정보를 로그에 남기지 않는다.
 */
@Slf4j(topic = "JwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationFilterProperties properties;

    private final List<PathPattern> excludePatterns;   // EXCLUDE: 완전 공개
    private final List<PathPattern> optionalPatterns;  // OPTIONAL: 선택적 인증
    private final List<PathPattern> includePatterns;   // REQUIRED: 필수 인증

    private final PathPatternParser parser = PathPatternParser.defaultInstance;

    public JwtAuthenticationFilter(JwtProvider jwtProvider,
            JwtAuthenticationFilterProperties properties) {
        this.jwtProvider = Objects.requireNonNull(jwtProvider, "jwtProvider");
        this.properties = Objects.requireNonNull(properties, "properties");

        this.excludePatterns = preparse(properties.getExcludePathPatterns());
        this.optionalPatterns = preparse(properties.getOptionalPathPatterns());
        this.includePatterns = preparse(properties.getIncludePathPatterns());
    }

    /**
     * 프로퍼티에 적어 둔 패턴 문자열을 PathPattern으로 파싱한다. "/foo/**" → "/foo"도 자동 포함, "/foo/" → "/foo"도 자동 포함.
     */
    private List<PathPattern> preparse(List<String> raws) {
        return raws == null
                ? List.of()
                : raws.stream()
                        .flatMap(this::expandPatternVariants)
                        .map(parser::parse)
                        .toList();
    }

    private Stream<String> expandPatternVariants(String pattern) {
        Stream<String> stream = Stream.of(pattern);

        // "/foo/**" → "/foo"도 매칭
        if (pattern.endsWith("/**") && pattern.length() > 3) {
            String trimmed = pattern.substring(0, pattern.length() - 3);
            if (StringUtils.hasText(trimmed)) {
                stream = Stream.concat(stream, Stream.of(trimmed));
            }
        }
        // "/foo/" → "/foo"도 매칭
        if (pattern.endsWith("/") && pattern.length() > 1) {
            stream = Stream.concat(stream, Stream.of(pattern.substring(0, pattern.length() - 1)));
        }
        return stream.distinct();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 모든 요청은 반드시 이 필터를 거친다.
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) 메서드 제외 (ex: OPTIONS 프리플라이트, HEAD 등)
        if (isExcludedMethod(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) 경로 정책 분류
        PathClass pathClass = classifyPath(request);

        // 3) EXCLUDE → 완전 공개
        if (pathClass == PathClass.EXCLUDE) {
            if (log.isTraceEnabled()) {
                log.trace("EXCLUDE path -> {}", request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 4) Access Token 추출 (헤더 우선 → 쿠키 fallback)
        String at = extractAtByHeaderOrCookie(request);

        // 5) REQUIRED → 토큰 필수
        if (pathClass == PathClass.REQUIRED) {
            if (!StringUtils.hasText(at)) {
                throw new AppException(AppErrorCode.TOKEN_REQUIRED);
            }
            try {
                Long userId = jwtProvider.getAtUserId(at);
                String role = jwtProvider.getAtUserRole(at); // enum을 쓰면 타입을 UserRole로 변경

                if (userId == null || !StringUtils.hasText(role)) {
                    throw new AppException(AppErrorCode.INVALID_TOKEN);
                }

                setCurrentUserAttributes(request, userId, role);

            } catch (TokenException te) {
                // JwtProvider 단계에서 발생한 만료/서명오류/클레임 오류 등
                throw new AppException(AppErrorCode.AUTHENTICATION_FAILED);
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 6) OPTIONAL → 토큰이 있으면 검증/세팅, 없거나 실패해도 통과
        if (!StringUtils.hasText(at)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Long userId = jwtProvider.getAtUserId(at);
            String role = jwtProvider.getAtUserRole(at);
            if (userId != null && StringUtils.hasText(role)) {
                setCurrentUserAttributes(request, userId, role);
            }
        } catch (TokenException ignore) {
            // OPTIONAL: 깨진 토큰이면 속성 세팅 없이 그냥 통과
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 경로 정책 분류 (우선순위: EXCLUDE > OPTIONAL > REQUIRED > DEFAULT) DEFAULT는
     * properties.getDefaultPathPattern()에서 정의한 정책을 따른다.
     */
    private PathClass classifyPath(HttpServletRequest request) {
        String strippedUri = stripContextPath(request.getRequestURI(), request.getContextPath());
        PathContainer pc = PathContainer.parsePath(strippedUri);

        if (matchesAny(pc, excludePatterns)) {
            return PathClass.EXCLUDE;
        }
        if (matchesAny(pc, optionalPatterns)) {
            return PathClass.OPTIONAL;
        }
        if (matchesAny(pc, includePatterns)) {
            return PathClass.REQUIRED;
        }

        // 나머지 전부는 기본 정책으로 처리 (REQUIRED 또는 EXCLUDE)
        return properties.getDefaultPathPattern(); // 프로퍼티가 PathClass(enum) 반환한다고 가정
    }

    private boolean matchesAny(PathContainer pc, List<PathPattern> patterns) {
        for (PathPattern p : patterns) {
            if (p.matches(pc)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcludedMethod(HttpServletRequest request) {
        List<String> excludeMethods = properties.getExcludeMethods();
        return excludeMethods != null
                && excludeMethods.stream().anyMatch(m -> m.equalsIgnoreCase(request.getMethod()));
    }

    private String stripContextPath(String uri, String ctx) {
        return (StringUtils.hasLength(ctx) && uri.startsWith(ctx)) ? uri.substring(ctx.length())
                : uri;
    }

    /**
     * AccessToken 추출 (헤더 우선, 없으면 쿠키 fallback). - Authorization: "Bearer <token>" (대소문자 무시) - 쿠키:
     * properties.cookieFallback=true일 때만 properties.atCookie 이름으로 검색
     */
    private String extractAtByHeaderOrCookie(HttpServletRequest request) {
        // 1) Authorization 헤더
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header)) {
            String candidate = header.trim();
            if (candidate.length() >= BEARER_PREFIX.length()
                    && candidate.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                String token = candidate.substring(BEARER_PREFIX.length()).trim();
                return StringUtils.hasText(token) ? token : null;
            }
        }

        // 2) 쿠키 fallback
        if (properties.isCookieFallback()) {
            String cookieName = properties.getAtCookie();
            if (request.getCookies() != null && StringUtils.hasText(cookieName)) {
                for (Cookie c : request.getCookies()) {
                    if (cookieName.equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                        return c.getValue().trim();
                    }
                }
            }
        }
        return null;
    }

    private void setCurrentUserAttributes(HttpServletRequest request, Long userId, String role) {
        request.setAttribute(AuthKeys.Attr.USER_ID, userId);
        request.setAttribute(AuthKeys.Attr.USER_ROLE, role);
        if (log.isDebugEnabled()) {
            log.debug("JWT OK -> userId={}, role={}, uri={}", userId, role,
                    request.getRequestURI());
        }
    }

    /**
     * 경로 정책
     */
    enum PathClass {
        EXCLUDE, OPTIONAL, REQUIRED
    }
}
