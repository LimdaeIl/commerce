package com.friday.commerce.order.domain.repository;

import com.friday.commerce.order.domain.entity.Order;
import com.friday.commerce.order.domain.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByOrderId(Long orderId);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(Long orderId);


    interface ListRow {
        Long getOrderId();
        LocalDateTime getCreatedAt();
        OrderStatus getOrderStatus();
        Integer getTotalAmount();
        Long getItemCount();

        String getPrimaryProductName();
        String getPrimaryImageUrl();
    }

    Page<ListRow> findOrderList(
            Long userId,
            String keyword,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

}
