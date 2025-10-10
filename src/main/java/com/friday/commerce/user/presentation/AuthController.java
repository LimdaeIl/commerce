package com.friday.commerce.user.presentation;

import com.friday.commerce.user.application.dto.auth.request.LogoutRequest;
import com.friday.commerce.user.application.dto.auth.request.ReIssueRequest;
import com.friday.commerce.user.application.dto.auth.request.SendCodeEmailRequest;
import com.friday.commerce.user.application.dto.auth.request.SignInRequest;
import com.friday.commerce.user.application.dto.auth.request.SignUpRequest;
import com.friday.commerce.user.application.dto.auth.request.VerifyCodeEmailRequest;
import com.friday.commerce.user.application.dto.auth.response.ReIssueResponse;
import com.friday.commerce.user.application.dto.auth.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.auth.response.SignInResponse;
import com.friday.commerce.user.application.dto.auth.response.SignUpResponse;
import com.friday.commerce.user.application.dto.auth.response.VerifyCodeEmailResponse;
import com.friday.commerce.user.application.usecase.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth-V1", description = "회원가입 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final AuthUseCase authUsecase;

    @Operation(
            summary = "회원가입",
            description = "회원 가입 요청 API 입니다. ...",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpRequest.class),
                            examples = @ExampleObject(
                                    name = "회원가입 요청 예시",
                                    value = """
                                            {
                                              "email": "alice@example.com",
                                              "password": "Aa!23456",
                                              "username": "앨리스123",
                                              "agreement": { "termsOfService": true, "privacy": true, "marketing": true },
                                              "address": {
                                                "zipCode": "06236",
                                                "addressLine1": "서울시 강남구 테헤란로 123",
                                                "addressLine2": "OO빌딩 5층",
                                                "city": "서울",
                                                "state": "강남구"
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignUpResponse.class),
                            examples = @ExampleObject(name = "회원가입 성공 예시",
                                    value = """
                                            {
                                              "userId": 6553614499713024,
                                              "email": "alice@example.com",
                                              "username": "앨리스123",
                                              "userRole": "USER",
                                              "createdAt": "2025-10-10T11:01:43.526496+09:00",
                                              "isLocked": false,
                                              "userAgreement": {
                                                "termsOfService": true,
                                                "privacy": true,
                                                "marketing": true,
                                                "agreedAt": "2025-10-10T11:01:43.457901+09:00"
                                              },
                                              "userAddresses": [
                                                {
                                                  "addressId": 1,
                                                  "zipCode": "06236",
                                                  "addressLine1": "서울시 강남구 테헤란로 123",
                                                  "addressLine2": "OO빌딩 5층",
                                                  "city": "서울",
                                                  "state": "강남구",
                                                  "isDefault": true
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(
            @RequestBody @Valid SignUpRequest request
    ) {
        SignUpResponse response = authUsecase.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @Operation(
            summary = "로그인",
            description = "로그인을 수행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignInResponse.class),
                            examples = @ExampleObject(name = "로그인 성공 예시",
                                    value = """
                                            {
                                              "userId": 6557027639758848,
                                              "at": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2NTU3MDI3NjM5NzU4ODQ4IiwiaWF0IjoxNzYwMDYyNTE5LCJleHAiOjE3NjAwOTg1MTksIlVTRVJfUk9MRSI6IlVTRVIiLCJqdGkiOiJhZjUzNTcyOC03MmU4LTQ1YjgtOTU4Yi0yMDJmNGQwOGI4NWMifQ.6Grh5DYisMvfAgo3MYpXcosdCESgAT3CXJC2Yr-If_A",
                                              "rt": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2NTU3MDI3NjM5NzU4ODQ4IiwiaWF0IjoxNzYwMDYyNTE5LCJleHAiOjE3NjAxNDg5MTksImp0aSI6ImJhYzRiZTM0LTA5YzQtNDNhYy05YjY1LTNjNTNmYWQ3Mzg3NCJ9.1CGKNC55vvqXEfEEEGPEKDUUzQDihWiE_sK3XtCXJLs",
                                              "atTtlMs": 35999753,
                                              "rtTtlMs": 86399752
                                            }
                                            """)))
    })
    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @RequestBody @Valid SignInRequest request
    ) {
        SignInResponse response = authUsecase.signIn(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "로그아웃",
            description = "로그아웃합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(hidden = true)))
    })
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

    @SecurityRequirement(name = "BearerAuth")
    @Operation(
            summary = "토큰 재발급",
            description = "토큰 재발급을 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReIssueResponse.class),
                            examples = @ExampleObject(name = "토큰 재발급 성공 예시",
                                    value = """
                                            {
                                              "userId": 6558496938004480,
                                              "newAt": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2NTU4NDk2OTM4MDA0NDgwIiwiaWF0IjoxNzYwMDYyOTk4LCJleHAiOjE3NjAwOTg5OTgsIlVTRVJfUk9MRSI6IlVTRVIiLCJqdGkiOiIzOWIzNDlhMC0yN2JhLTQwOGMtOThkYi1iZWM5MmNmNDIxMmEifQ.fgH-U68gJPF6IvS1oEGbl4zmoNCzh-v4UGW2w_H3ZS4",
                                              "newRt": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2NTU4NDk2OTM4MDA0NDgwIiwiaWF0IjoxNzYwMDYyOTk4LCJleHAiOjE3NjAxNDkzOTgsImp0aSI6Ijk3ZjQzODc3LTdmM2YtNDNjOC04ZjAxLTAzYWIzNmIzN2JjYyJ9.wP7PQJYFhn5NydXJRHutggVQw72msi0pZOKG2CGBKMw",
                                              "newAtTtlMs": 35999379,
                                              "newRtTtlMs": 86399378
                                            }
                                            """)))
    })
    @PostMapping("/token-reissue")
    public ResponseEntity<ReIssueResponse> reissue(
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authHeader,
            @RequestBody @Valid ReIssueRequest request
    ) {
        ReIssueResponse response = authUsecase.reIssue(authHeader, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "이메일 인증 코드 전송",
            description = "입력한 이메일에 인증 코드를 전송 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 코드 전송 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SendCodeEmailResponse.class),
                            examples = @ExampleObject(name = "이메일 인증 코드 전송 성공 예시",
                                    value = """
                                            {
                                              "recipient": "alice@example.com",
                                              "validMinutes": 5
                                            }
                                            """)))
    })
    @PostMapping("/email/send-code")
    public ResponseEntity<SendCodeEmailResponse> sendCodeEmail(
            @Valid @RequestBody SendCodeEmailRequest request
    ) {
        SendCodeEmailResponse response = authUsecase.sendCodeEmail(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "이메일 인증 코드 검증",
            description = "입력한 이메일에 도착한 인증 코드를 검증 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 코드 검증 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VerifyCodeEmailResponse.class),
                            examples = @ExampleObject(name = "이메일 인증 코드 전송 검증 예시",
                                    value = """
                                            {
                                              "recipient": "alice@example.com",
                                              "verifiedUntilEpochMillis": 1760063840952
                                            }
                                            """)))
    })
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
