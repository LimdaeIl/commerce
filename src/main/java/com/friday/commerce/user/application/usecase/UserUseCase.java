package com.friday.commerce.user.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.user.application.dto.auth.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailConfirmRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailRequest;
import com.friday.commerce.user.application.dto.user.request.UpdatePasswordRequest;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;
import jakarta.validation.Valid;

public interface UserUseCase {

    GetUserResponse getUser(CurrentUserInfo info);

    void updatePassword(String authHeader, CurrentUserInfo info, UpdatePasswordRequest request);

    SendCodeEmailResponse updateEmail(String authHeader, CurrentUserInfo info, UpdateEmailRequest request);

    void confirmUpdateEmail(String authHeader, CurrentUserInfo info, UpdateEmailConfirmRequest request);
}
