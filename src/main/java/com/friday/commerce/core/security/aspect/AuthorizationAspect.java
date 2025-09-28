package com.friday.commerce.core.security.aspect;

import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.core.web.exception.AppErrorCode;
import com.friday.commerce.core.web.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j(topic = "AuthorizationAspect")
@Aspect
@Component
public class AuthorizationAspect {

    private final String ATTR_USER_ROLE = "X-User-Role";

    @Before("@within(com.friday.commerce.core.security.annotation.RequireRole) || @annotation(com.friday.commerce.core.security.annotation.RequireRole)")
    public void requireRole(JoinPoint jp) {
        RequireRole requireRole = resolveRequireRole(jp);
        if (requireRole == null) {
            return; // 애너테이션이 없으면 권한 체크 생략
        }

        if (requireRole.allowAnonymous()) {
            return; // 익명 허용인 경우 바로 통과
        }

        UserRole currentUserRole = currentUserRole(); // 권한 없으면 401 발생

        UserRole[] required = requireRole.value();
        if (required.length == 0) {
            return;
        }

        Set<UserRole> allowedUserRole = Arrays.stream(required).collect(Collectors.toSet());

        if (!allowedUserRole.contains(currentUserRole)) {
            log.info("포함되지 않은 권한 거부: 현재 권한: {}, 허용 권한: {}", currentUserRole,
                    Arrays.toString(required));
            throw new AppException(AppErrorCode.FORBIDDEN);
        }


    }

    private RequireRole resolveRequireRole(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        RequireRole annotation = signature.getMethod().getAnnotation(RequireRole.class);

        if (annotation != null) {
            return annotation;
        }

        return jp.getTarget().getClass().getAnnotation(RequireRole.class);
    }

    private UserRole currentUserRole() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw new AppException(AppErrorCode.REQUEST_CONTEXT_NOT_FOUND);
        }

        HttpServletRequest request = attrs.getRequest();
        Object roleAttr = request.getAttribute(ATTR_USER_ROLE);

        if (roleAttr == null) {
            throw new AppException(AppErrorCode.MISSING_HEADER_USER_ROLE);
        }

        if (!(roleAttr instanceof UserRole userRole)) {
            throw new AppException(AppErrorCode.INVALID_HEADER_USER_ROLE);
        }

        return userRole;
    }
}
