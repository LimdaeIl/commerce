package com.friday.commerce.order.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.order.application.dto.request.CreateOrderRequest;
import com.friday.commerce.order.application.dto.response.CreateOrderResponse;

public interface OrderUseCase {

    CreateOrderResponse createOrder(CreateOrderRequest request, CurrentUserInfo info);
}
