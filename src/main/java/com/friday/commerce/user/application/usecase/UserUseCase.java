package com.friday.commerce.user.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.user.application.dto.user.request.UpdatePasswordRequest;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;

public interface UserUseCase {

    GetUserResponse getUser(CurrentUserInfo info);

    void updatePassword(String authHeader, CurrentUserInfo info, UpdatePasswordRequest request);
}
