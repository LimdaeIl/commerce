package com.friday.commerce.user.application.dto.auth.request;

import jakarta.validation.constraints.NotNull;

public record LogoutRequest(
        @NotNull
        String rt
) {

}
