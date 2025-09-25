package com.friday.commerce.user.application.usecase;

import com.friday.commerce.user.application.dto.request.SignUpRequest;
import com.friday.commerce.user.application.dto.response.SignUpResponse;

public interface UserUseCase {

    SignUpResponse signUp(SignUpRequest request);
}
