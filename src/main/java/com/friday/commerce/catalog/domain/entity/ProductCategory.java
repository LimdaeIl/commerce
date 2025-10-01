package com.friday.commerce.catalog.domain.entity;

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
@Table(name = "product_categories")
@Entity
public class ProductCategory {

    @Id
    @Column(name = "product_category_id", nullable = false, updatable = false)
    private Long productCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    private ProductCategory(Long productCategoryId, Product product, Category category) {
        this.productCategoryId = productCategoryId;
        this.product = product;
        this.category = category;
    }

    public static ProductCategory link(Long productCategoryId, Product product, Category category) {
        return new ProductCategory(productCategoryId, product, category);
    }

    void assignTo(Product product) {
        this.product = product;
    }

}
