package com.friday.commerce.user.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;

public interface UserUseCase {

    GetUserResponse getUser(CurrentUserInfo info);
}
