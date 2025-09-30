package com.friday.commerce.user.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.user.application.dto.auth.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.user.request.RegisterAddressRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailConfirmRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailRequest;
import com.friday.commerce.user.application.dto.user.request.UpdatePasswordRequest;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;

public interface UserUseCase {

    GetUserResponse getUser(CurrentUserInfo info);

    void updatePassword(String authHeader, CurrentUserInfo info, UpdatePasswordRequest request);

    SendCodeEmailResponse updateEmail(CurrentUserInfo info, UpdateEmailRequest request);

    void confirmUpdateEmail(String authHeader, CurrentUserInfo info,
            UpdateEmailConfirmRequest request);

    GetUserResponse registerAddress(CurrentUserInfo info, RegisterAddressRequest request);

    GetUserResponse updateDefaultAddress(CurrentUserInfo info, Long addressId);

    GetUserResponse deleteAddress(CurrentUserInfo info, Long addressId);
}
