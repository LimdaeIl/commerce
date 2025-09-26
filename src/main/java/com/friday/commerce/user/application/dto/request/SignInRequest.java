package com.friday.commerce.user.application.dto.request;

public record SignInRequest(
        String email,
        String password
) {

}
