package com.friday.commerce.order.infrastructure.jpa;

import com.friday.commerce.order.domain.entity.Order;
import com.friday.commerce.order.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long>, OrderRepository {

}
