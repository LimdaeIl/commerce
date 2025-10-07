package com.friday.commerce.order.domain.repository;

import com.friday.commerce.order.domain.entity.Order;

public interface OrderRepository {

    Order save(Order order);
}
