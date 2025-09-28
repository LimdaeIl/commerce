package com.friday.commerce.core.security.model;

public record CurrentUserInfo(
        Long userId,
        UserRole userRole
) {

    public static CurrentUserInfo of(Long userId, UserRole userRole) {
        return new CurrentUserInfo(userId, userRole);
    }

}
