package com.friday.commerce.user.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.user.application.dto.auth.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailConfirmRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailRequest;
import com.friday.commerce.user.application.dto.user.request.UpdatePasswordRequest;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;
import com.friday.commerce.user.application.usecase.UserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserUseCase userUseCase;

    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getMe(
            @CurrentUser CurrentUserInfo info
    ) {
        GetUserResponse response = userUseCase.getUser(info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authHeader,
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid UpdatePasswordRequest request
    ) {
        userUseCase.updatePassword(authHeader, info, request);

        return ResponseEntity
                .noContent()
                .build();
    }

    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PatchMapping("/email")
    public ResponseEntity<SendCodeEmailResponse> updateEmail(
            @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authHeader,
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid UpdateEmailRequest request
            ) {
        SendCodeEmailResponse response = userUseCase.updateEmail(authHeader, info, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/v1/users/email/confirm")
    public ResponseEntity<Void> confirmEmailChange(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CurrentUser CurrentUserInfo info,
            @Valid @RequestBody UpdateEmailConfirmRequest request
    ) {
        userUseCase.confirmUpdateEmail(authHeader, info, request);
        return ResponseEntity.noContent().build();
    }

}
