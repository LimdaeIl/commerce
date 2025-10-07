package com.friday.commerce.order.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.order.application.dto.request.CreateOrderRequest;
import com.friday.commerce.order.application.dto.response.CreateOrderResponse;
import com.friday.commerce.order.application.usecase.OrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@RestController
public class OrderController {

    private final OrderUseCase orderUseCase;

    @RequireRole({UserRole.ADMIN, UserRole.SELLER, UserRole.USER})
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @CurrentUser CurrentUserInfo info
    ) {
        CreateOrderResponse response = orderUseCase.createOrder(request, info);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
