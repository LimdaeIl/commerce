package com.friday.commerce.order.application.usecase;

import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.web.response.PageResponse;
import com.friday.commerce.order.application.dto.request.CreateOrderRequest;
import com.friday.commerce.order.application.dto.response.CreateOrderResponse;
import com.friday.commerce.order.application.dto.response.GetOrderResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface OrderUseCase {

    CreateOrderResponse createOrder(CreateOrderRequest request, CurrentUserInfo info);

    PageResponse<GetOrderResponse> getOrders(CurrentUserInfo info,  String keyword, LocalDate from, LocalDate to, Pageable pageable);
}
