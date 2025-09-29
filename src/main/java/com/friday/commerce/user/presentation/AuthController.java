package com.friday.commerce.user.presentation;

import com.friday.commerce.user.application.dto.request.LogoutRequest;
import com.friday.commerce.user.application.dto.request.ReIssueRequest;
import com.friday.commerce.user.application.dto.request.SendCodeEmailRequest;
import com.friday.commerce.user.application.dto.request.SignInRequest;
import com.friday.commerce.user.application.dto.request.SignUpRequest;
import com.friday.commerce.user.application.dto.request.VerifyCodeEmailRequest;
import com.friday.commerce.user.application.dto.response.ReIssueResponse;
import com.friday.commerce.user.application.dto.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.response.SignInResponse;
import com.friday.commerce.user.application.dto.response.SignUpResponse;
import com.friday.commerce.user.application.dto.response.VerifyCodeEmailResponse;
import com.friday.commerce.user.application.usecase.AuthUsecase;
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

    private final AuthUsecase authUsecase;

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(
            @RequestBody @Valid SignUpRequest request
    ) {
        SignUpResponse response = authUsecase.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @RequestBody @Valid SignInRequest request
    ) {
        SignInResponse response = authUsecase.signIn(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestBody @Valid LogoutRequest request
    ) {
        authUsecase.logout(authHeader, request);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/token-reissue")
    public ResponseEntity<ReIssueResponse> reissue(
            @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authHeader,
            @RequestBody @Valid ReIssueRequest request
    ) {
        ReIssueResponse response = authUsecase.reIssue(authHeader, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @PostMapping("/email/send-code")
    public ResponseEntity<SendCodeEmailResponse> SendCodeEmail(
            @Valid @RequestBody SendCodeEmailRequest request
    ) {
        SendCodeEmailResponse response = authUsecase.sendCodeEmail(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<VerifyCodeEmailResponse> verifyCodeEmail(
            @Valid @RequestBody VerifyCodeEmailRequest request
    ) {
        VerifyCodeEmailResponse response = authUsecase.verifyCodeEmail(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
