package com.friday.commerce.order.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
@Entity
public class OrderItem {

    @Id
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private Long orderItemId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_name", nullable = false)
    private String productName; // 주문 시점의 상품 이름 스냅샷

    @Column(name = "product_price", nullable = false)
    private Integer productPrice; // 주문 시점의 상품 가격 스냅샷

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // productPrice * quantity

    public OrderItem(
            Long orderItemId,
            Long productId,
            Order order,
            String productName,
            Integer productPrice,
            Integer quantity,
            Integer totalPrice
    ) {
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.order = order;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public static OrderItem create(
            Long orderItemId,
            Long productId,
            Order order,
            String productName,
            Integer productPrice,
            Integer quantity,
            Integer totalPrice
    ) {
        return new OrderItem(
                orderItemId,
                productId,
                order,
                productName,
                productPrice,
                quantity,
                totalPrice
        );
    }
}
