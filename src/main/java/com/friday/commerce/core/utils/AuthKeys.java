package com.friday.commerce.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthKeys {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Header {
        public static final String HDR_USER_ID = "X-User-Id";
        public static final String HDR_USER_ROLE = "X-User-Role";
    }
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Attr {
        public static final String USER_ID = "auth.userId";
        public static final String USER_ROLE = "auth.userRole";
    }
}
