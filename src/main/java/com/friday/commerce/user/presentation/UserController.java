package com.friday.commerce.user.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;
import com.friday.commerce.user.application.usecase.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

}
