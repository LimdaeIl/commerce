package com.friday.commerce.user.application.usecase;

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

public interface AuthUseCase {

    SignUpResponse signUp(SignUpRequest request);

    SignInResponse signIn(SignInRequest request);

    void logout(String authHeader, LogoutRequest request);

    ReIssueResponse reIssue(String authHeader, ReIssueRequest request);

    SendCodeEmailResponse sendCodeEmail(SendCodeEmailRequest request);

    VerifyCodeEmailResponse verifyCodeEmail(VerifyCodeEmailRequest request);
}
