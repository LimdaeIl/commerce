package com.friday.commerce.core.web.resolver;

import static com.friday.commerce.core.utils.AuthKeys.Attr.USER_ID;
import static com.friday.commerce.core.utils.AuthKeys.Attr.USER_ROLE;
import static com.friday.commerce.core.utils.AuthKeys.Header.HDR_USER_ID;
import static com.friday.commerce.core.utils.AuthKeys.Header.HDR_USER_ROLE;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.core.web.exception.AppErrorCode;
import com.friday.commerce.core.web.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j(topic = "CurrentUserArgumentResolver")
@RequiredArgsConstructor
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Value("${security.current-user.allow-header-fallback:false}")
    private boolean allowHeaderFallback;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(CurrentUser.class)) {
            return false;
        }

        Class<?> type = parameter.getParameterType();
        if (CurrentUserInfo.class.isAssignableFrom(type)) {
            return true;
        }

        if (Optional.class.isAssignableFrom(type)) {
            Type g = parameter.getGenericParameterType();
            if (g instanceof ParameterizedType pt) {
                Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class<?> c && CurrentUserInfo.class.isAssignableFrom(c)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        if (req == null) {
            throw new AppException(AppErrorCode.REQUEST_CONTEXT_NOT_FOUND);
        }

        boolean wantsOptional = Optional.class.isAssignableFrom(parameter.getParameterType());

        CurrentUserInfo info = extractCurrentUser(req);

        if (wantsOptional) {
            return Optional.ofNullable(info);
        }

        if (info == null) {
            // 인증 정보가 전혀 없는 경우(익명) → 401
            throw new AppException(AppErrorCode.UNAUTHORIZED);
        }
        return info;
    }

    /**
     * attribute 우선, 필요 시(설정에 따라) header fallback
     */
    private CurrentUserInfo extractCurrentUser(HttpServletRequest req) {
        Object idAttr = req.getAttribute(USER_ID);
        Object roleAttr = req.getAttribute(USER_ROLE);

        Long userId = coerceUserId(idAttr);
        UserRole role = coerceUserRole(roleAttr);

        if (userId == null || role == null) {
            if (allowHeaderFallback) {
                if (userId == null) {
                    userId = parseUserIdHeader(req.getHeader(HDR_USER_ID));
                }
                if (role == null) {
                    role = parseUserRoleHeader(req.getHeader(HDR_USER_ROLE));
                }
            }
        }

        if (userId == null && role == null) {
            return null; // 완전 익명
        }
        if (userId == null) {
            throw new AppException(AppErrorCode.MISSING_HEADER_USER_ID);
        }
        if (role == null) {
            throw new AppException(AppErrorCode.MISSING_HEADER_USER_ROLE);
        }

        try {
            return CurrentUserInfo.of(userId, role);
        } catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_USER_INFO);
        }
    }

    private Long coerceUserId(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Long l) {
            return l;
        }
        if (v instanceof Integer i) {
            return i.longValue();
        }
        if (v instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private UserRole coerceUserRole(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof UserRole r) {
            return r;
        }
        if (v instanceof String s) {
            return toUserRoleOrNull(s);
        }
        return null;
    }

    private Long parseUserIdHeader(String headerVal) {
        if (headerVal == null || headerVal.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(headerVal.trim());
        } catch (NumberFormatException e) {
            throw new AppException(AppErrorCode.INVALID_HEADER_USER_ID_NOT_INTEGER);
        }
    }

    private UserRole parseUserRoleHeader(String headerVal) {
        if (headerVal == null || headerVal.isBlank()) {
            return null;
        }
        UserRole userRoleOrNull = toUserRoleOrNull(headerVal);
        if (userRoleOrNull == null) {
            throw new AppException(AppErrorCode.NULL_USER_ROLE);
        }
        return userRoleOrNull;
    }

    private UserRole toUserRoleOrNull(String s) {
        try {
            return UserRole.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}