package com.friday.commerce.user.presentation;

import com.friday.commerce.user.application.dto.request.LogoutRequest;
import com.friday.commerce.user.application.dto.request.SignInRequest;
import com.friday.commerce.user.application.dto.request.SignUpRequest;
import com.friday.commerce.user.application.dto.response.SignInResponse;
import com.friday.commerce.user.application.dto.response.SignUpResponse;
import com.friday.commerce.user.application.usecase.UserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final UserUseCase userUseCase;

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(
        @RequestBody @Valid SignUpRequest request
    ) {
        SignUpResponse response = userUseCase.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @RequestBody @Valid SignInRequest request
    ) {
        SignInResponse response = userUseCase.signIn(request);

        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestBody @Valid LogoutRequest request
    ) {
        userUseCase.logout(authHeader, request);

        return ResponseEntity
                .noContent()
                .build();
    }

}
