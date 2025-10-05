package com.friday.commerce.order.domain.entity;

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
        this.ordererAddress = ordererAddress;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedAt = null;
        this.updatedBy = null;
        this.deletedAt = null;
        this.deletedBy = null;

        if (!orderItems.isEmpty() || ordererAddress != null) {
            this.orderItems = orderItems;
        }
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
}
