package com.friday.commerce.user.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record LogoutRequest(
        @NotNull
        String rt
) {

}
