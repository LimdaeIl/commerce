package com.friday.commerce.catalog.domain.entity;

import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_skus")
@Entity
public class ProductSku {

    @Id
    @Column(name = "product_sku_id", nullable = false, updatable = false)
    private Long productSkuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "stock", nullable = false)
    private Long stock;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductSku(
            Long productSkuId,
            Product product,
            Long price,
            Long stock
    ) {
        this.productSkuId = productSkuId;
        this.product = product;
        this.price = price;
        this.stock = stock;
    }

    public static ProductSku create(
            Long productSkuId,
            Product product,
            Long price,
            Long stock
    ) {
        return ProductSku.builder()
                .productSkuId(productSkuId)
                .product(product)
                .price(price)
                .stock(stock)
                .build();
    }

    void assignTo(Product product) {
        this.product = product;
    }

    public void increment(long quantity) {
        if (quantity <= 0) {
            throw new ProductException(ProductErrorCode.SKU_INVALID_QUANTITY);
        }
        this.stock += quantity;
    }

    public void decrement(long quantity) {
        if (quantity <= 0) {
            throw new ProductException(ProductErrorCode.SKU_INVALID_QUANTITY);
        }

        if (quantity > stock) {
            throw new ProductException(ProductErrorCode.SKU_STOCK_INSUFFICIENT);
        }

        this.stock -= quantity;
    }
}
