package com.friday.commerce.order.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.core.web.response.PageResponse;
import com.friday.commerce.order.application.dto.request.CreateOrderRequest;
import com.friday.commerce.order.application.dto.response.CreateOrderResponse;
import com.friday.commerce.order.application.dto.response.GetOrderResponse;
import com.friday.commerce.order.application.usecase.OrderUseCase;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    @RequireRole({UserRole.ADMIN, UserRole.SELLER, UserRole.USER})
    @GetMapping
    public ResponseEntity<PageResponse<GetOrderResponse>> getOrders(
            @CurrentUser CurrentUserInfo info,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Direction.DESC)
            }) Pageable pageable
    ) {
        PageResponse<GetOrderResponse> response = orderUseCase.getOrders(info, keyword, from, to, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
