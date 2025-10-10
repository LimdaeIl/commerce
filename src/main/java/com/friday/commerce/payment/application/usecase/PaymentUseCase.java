package com.friday.commerce.payment.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.payment.application.dto.response.ConfirmPaymentResponse;
import com.friday.commerce.payment.application.dto.request.ConfirmPaymentRequest;

public interface PaymentUseCase {

    ConfirmPaymentResponse confirm(ConfirmPaymentRequest request, CurrentUserInfo info);
}
