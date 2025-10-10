package com.friday.commerce.payment.infrastructure.external;


import com.friday.commerce.order.application.facade.OrderFacade;
import com.friday.commerce.payment.application.port.out.OrderPort;
import com.friday.commerce.payment.domain.entity.OrderBrief;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderAdapter implements OrderPort {

    private final OrderFacade orderFacade;

    @Override
    public Optional<OrderBrief> findByOrderId(Long orderId) {
        return orderFacade.findOrderBrief(orderId)
                .map(response -> new OrderBrief(
                        response.orderId(),
                        response.userId(),
                        response.totalAmount(),
                        response.status().name()
                ));
    }

    @Override
    public void markPaid(Long orderId, Long userId) {
        orderFacade.markPaid(orderId, userId);
    }
}
