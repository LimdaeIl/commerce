package com.friday.commerce.payment.application.port.out;

import com.friday.commerce.payment.domain.entity.OrderBrief;
import java.util.Optional;

public interface OrderPort {

    Optional<OrderBrief> findByOrderId(Long orderId);

    void markPaid(Long orderId, Long userId);
}
