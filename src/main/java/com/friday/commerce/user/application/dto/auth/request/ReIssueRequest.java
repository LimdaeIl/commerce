package com.friday.commerce.user.application.dto.auth.request;

import jakarta.validation.constraints.NotNull;

public record ReIssueRequest(
        @NotNull
        String rt
) {

}
