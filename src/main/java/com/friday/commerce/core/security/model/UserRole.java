package com.friday.commerce.core.security.model;

import com.friday.commerce.core.web.exception.AppErrorCode;
import com.friday.commerce.core.web.exception.AppException;
import java.util.Locale;

public enum UserRole {
    USER,
    SELLER,
    ADMIN;


    // JWT 안의 role 문자열을 파싱 → 실패 시 401 INVALID_TOKEN
    public static UserRole parseForToken(String raw) {
        try {
            return parse(raw);
        } catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_TOKEN);
        }
    }

    // 헤더/요청 속성의 role 문자열을 파싱 → 실패 시 400 INVALID_HEADER_USER_ROLE
    public static UserRole parseForHeader(String raw) {
        try {
            return parse(raw);
        } catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_HEADER_USER_ROLE);
        }
    }

     // 공통 파싱 로직: ROLE_ 접두어/대소문자/공백 허용
    private static UserRole parse(String raw) {
        if (raw == null) {
            throw new AppException(AppErrorCode.NULL_USER_ROLE);
        }

        String t = raw.trim();
        if (t.isEmpty()) {
            throw new AppException(AppErrorCode.EMPTY_USER_ROLE);
        }

        t = t.toUpperCase(Locale.ROOT);
        if (t.startsWith("ROLE_")) {
            t = t.substring(5);
        }

        return UserRole.valueOf(t);
    }
}
