package com.friday.commerce.user.application.usecase;

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

public interface UserUseCase {

    SignUpResponse signUp(SignUpRequest request);

    SignInResponse signIn(SignInRequest request);

    void logout(String authHeader, LogoutRequest request);

    ReIssueResponse reIssue(String authHeader, ReIssueRequest request);

    SendCodeEmailResponse sendCodeEmail(SendCodeEmailRequest request);

    VerifyCodeEmailResponse verifyCodeEmail(VerifyCodeEmailRequest request);
}
