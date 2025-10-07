package com.friday.commerce.order.application.facade;

import com.friday.commerce.order.application.dto.response.OrderBriefResponse;
import java.util.Optional;

public interface OrderFacade {

    Optional<OrderBriefResponse> findOrderBrief(Long orderId);

    void markPaid(Long orderId, Long userId);
}
