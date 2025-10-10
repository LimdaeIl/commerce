package com.friday.commerce.catalog.domain.entity;

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
@Table(name = "product_images")
@Entity
public class ProductImage {

    @Id
    @Column(name = "product_image_id", nullable = false, updatable = false)
    private Long productImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "caption", length = 200)
    private String caption;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder(access = AccessLevel.PRIVATE)
    public ProductImage(
            Long productImageId,
            Product product,
            String imageUrl,
            String caption,
            int sortOrder) {
        this.productImageId = productImageId;
        this.product = product;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.sortOrder = sortOrder;
    }

    public static ProductImage create(
            Long productImageId,
            Product product,
            String imageUrl,
            String caption,
            int sortOrder
    ) {
        return ProductImage.builder()
                .productImageId(productImageId)
                .product(product)
                .imageUrl(imageUrl)
                .caption(caption)
                .sortOrder(sortOrder)
                .build();
    }

    public void assignTo(Product product) {
        this.product = product;
    }

}
