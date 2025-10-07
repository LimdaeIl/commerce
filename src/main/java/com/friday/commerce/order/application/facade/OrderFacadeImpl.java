package com.friday.commerce.order.application.facade;

import com.friday.commerce.order.application.dto.response.OrderBriefResponse;
import com.friday.commerce.order.domain.exception.OrderErrorCode;
import com.friday.commerce.order.domain.exception.OrderException;
import com.friday.commerce.order.domain.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderFacadeImpl implements OrderFacade {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    @Override
    public Optional<OrderBriefResponse> findOrderBrief(Long orderId) {
        return orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .map(o -> new OrderBriefResponse(
                        o.getOrderId(),
                        o.getUserId(),
                        o.getTotalAmount(),
                        o.getOrderStatus()
                ));
    }

    @Transactional
    @Override
    public void markPaid(Long orderId, Long userId) {
        var order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.markPaid(userId); // 도메인 전이 호출 → 더티체킹으로 UPDATE 반영
    }
}
