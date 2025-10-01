package com.friday.commerce.catalog.domain.entity;

import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "products")
@Entity
public class Product {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSku> productSkus = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

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
    private Product(
            Long productId,
            String title,
            String content,
            ProductStatus status,
            LocalDateTime createdAt,
            Long createdBy
    ) {
        this.productId = productId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public static Product create(
            Long productId,
            String title,
            String content,
            Long createdBy
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Product.builder()
                .productId(productId)
                .title(title)
                .content(content)
                .status(ProductStatus.DRAFT)
                .createdAt(now)
                .createdBy(createdBy)
                .build();
    }

    public void addSku(ProductSku sku) {
        sku.assignTo(this);
        this.productSkus.add(sku);
    }

    public void replaceSkus(List<ProductSku> skus) {
        this.productSkus.clear();

        if (skus == null) {
            return;
        }

        for (ProductSku s : skus) {
            addSku(s);
        }
    }

    public void addImage(ProductImage image) {
        image.assignTo(this);
        this.images.add(image);
    }

    public void replaceImages(List<ProductImage> images) {
        this.images.clear();

        if (images == null) {
            return;
        }

        for (ProductImage image : images) {
            addImage(image);
        }
    }

    public void addProductCategory(ProductCategory link) {
        link.assignTo(this);
        this.productCategories.add(link);
    }

    public void replaceCategories(List<ProductCategory> newLinks) {
        this.productCategories.clear();

        if (newLinks == null) {
            return;
        }

        for (ProductCategory pc : newLinks) {
            pc.assignTo(this); 
            this.productCategories.add(pc);
        }
    }

    public void publish(Long userId) {
        if (this.status == ProductStatus.PUBLISHED) {
            throw new ProductException(ProductErrorCode.PRODUCT_STATUS_SAME_BEFORE);
        }

        this.status = ProductStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void archive(Long userId) {
        if (this.status == ProductStatus.ARCHIVED) {
            throw new ProductException(ProductErrorCode.PRODUCT_STATUS_SAME_BEFORE);
        }

        this.status = ProductStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
}
