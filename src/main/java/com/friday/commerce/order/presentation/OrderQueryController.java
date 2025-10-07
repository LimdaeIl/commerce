package com.friday.commerce.order.presentation;

import com.friday.commerce.order.application.dto.response.OrderBriefResponse;
import com.friday.commerce.order.application.facade.OrderFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderQueryController {

    private final OrderFacade orderFacade;

    @GetMapping("/{orderId}/brief")
    public ResponseEntity<OrderBriefResponse> getOrderBrief(@PathVariable Long orderId) {
        return orderFacade.findOrderBrief(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}