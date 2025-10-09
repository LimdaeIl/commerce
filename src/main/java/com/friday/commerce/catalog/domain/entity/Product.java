package com.friday.commerce.catalog.domain.entity;

import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductSku productSku;

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
    /* ---------- 이미지/카테고리 편의 메서드 기존 유지 ---------- */

    public void addImage(ProductImage image) {
        image.assignTo(this);
        this.images.add(image);
    }

    public void replaceImages(List<ProductImage> images) {
        this.images.forEach(image -> image.assignTo(null));
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
        this.productCategories.forEach(link -> link.assignTo(null));
        this.productCategories.clear();

        if (newLinks == null) {
            return;
        }
        for (ProductCategory pc : newLinks) {
            pc.assignTo(this);
        }
    }

    public void setSku(ProductSku newSku) {
        // 기존 연결 제거
        if (this.productSku != null) {
            this.productSku.setProduct(null);
        }

        this.productSku = newSku;

        // 역방향 연결
        if (newSku != null && newSku.getProduct() != this) {
            newSku.setProduct(this); // 소유측에 FK 세팅
        }
    }

    public void draft(Long userId) {
        if (this.status == ProductStatus.DRAFT) {
            throw new ProductException(ProductErrorCode.PRODUCT_STATUS_SAME_BEFORE);
        }
        this.status = ProductStatus.DRAFT;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
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

    public void increaseStock(long quantity) {
        ProductSku sku = ensureSku();
        sku.increment(quantity);
    }

    public void decreaseStock(long quantity) {
        ProductSku sku = ensureSku();
        sku.decrement(quantity);
    }

    private ProductSku ensureSku() {
        if (this.productSku == null) {
            throw new ProductException(ProductErrorCode.SKU_NOT_FOUND);
        }
        return this.productSku;
    }

    public void softDelete(Long userId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }

    public void productStatusArchived(Long userId) {
        this.status = ProductStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void updateTitle(String newTitle, Long userId) {
        if (newTitle == null) {
            return;
        }
        String t = newTitle.trim();
        if (t.isBlank()) {
            throw new ProductException(ProductErrorCode.PRODUCT_TITLE_EMPTY);
        }
        if (t.length() > 150) {
            throw new ProductException(ProductErrorCode.PRODUCT_TITLE_LENGTH_EXCEEDED);
        }
        this.title = t;
        this.updatedAt = java.time.LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void updateContent(String newContent, Long userId) {
        if (newContent == null) {
            return;
        }
        String c = newContent.trim();
        if (c.isBlank()) {
            throw new ProductException(ProductErrorCode.PRODUCT_CONTENT_EMPTY);
        }
        this.content = c;
        this.updatedAt = java.time.LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void changeStatus(ProductStatus target, Long userId) {
        if (target == null || target == this.status) {
            return;
        }
        switch (target) {
            case DRAFT -> this.draft(userId);
            case PUBLISHED -> this.publish(userId);
            case ARCHIVED -> this.archive(userId);
        }
        this.updatedAt = java.time.LocalDateTime.now();
        this.updatedBy = userId;
    }

    public void touch(Long userId) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }


}
