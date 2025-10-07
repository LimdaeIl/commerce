package com.friday.commerce.order.domain.entity;

import com.friday.commerce.order.domain.exception.OrderErrorCode;
import com.friday.commerce.order.domain.exception.OrderException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "user_id",  nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private OrdererAddress ordererAddress;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(
            Long orderId,
            Long userId,
            OrderStatus orderStatus,
            List<OrderItem> orderItems,
            OrdererAddress ordererAddress,
            Integer totalAmount,
            Long createdBy
    ) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems != null ? new ArrayList<>(orderItems) : new ArrayList<>();
        this.ordererAddress = ordererAddress;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedAt = null;
        this.updatedBy = null;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public static Order create(
            Long orderId,
            Long userId,
            OrderStatus orderStatus,
            List<OrderItem> orderItems,
            OrdererAddress ordererAddress,
            Integer totalAmount,
            Long createdBy
    ) {
        return Order.builder()
                .orderId(orderId)
                .userId(userId)
                .orderStatus(orderStatus)
                .orderItems(orderItems)
                .ordererAddress(ordererAddress)
                .totalAmount(totalAmount)
                .createdBy(createdBy)
                .build();
    }

    public void markPaid(Long actorId) {
        if (this.deletedAt != null) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        if (this.orderStatus == OrderStatus.PAID) {
            // 멱등: 이미 PAID면 그냥 조용히 리턴해도 됨 (선호) return;
            throw new OrderException(OrderErrorCode.ORDER_ALREADY_PAID);
        }
        if (this.orderStatus != OrderStatus.CREATED) {
            throw new OrderException(OrderErrorCode.ORDER_STATUS_INVALID);
        }
        this.orderStatus = OrderStatus.PAID;
        this.updatedAt = java.time.LocalDateTime.now();
        this.updatedBy = actorId;
    }
}
