package com.friday.commerce.catalog.domain.entity;


import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "category_id", nullable = false, updatable = false)
    private Long categoryId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Adjacency List: 자식 카테고리 목록
    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    // Materialized Path: 빠른 하위 카테고리 조회를 위한 경로
    // 예: 상위(1) > 중위(15) > 하위(23) -> "1/15/23/"
    // 인덱스 필수!
    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int depth;

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
    private Category(
            Long categoryId,
            String name,
            Category parent,
            Long createdBy
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.parent = parent;

        if (parent == null) { // null: Root(1) 인 경우
            this.depth = 1;
            this.path = categoryId + "/";
        } else {
            this.depth = parent.getDepth() + 1;
            this.path = parent.getPath() + categoryId + "/";
        }
        this.createdAt = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedAt = null;
        this.updatedBy = null;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public static Category createRoot(Long categoryId, String name, Long createdBy) {
        return Category.builder()
                .categoryId(categoryId)
                .name(name)
                .parent(null)
                .createdBy(createdBy)
                .build();
    }

    public static Category createChild(Long categoryId, String name, Category parent,  Long createdBy) {
        if (parent == null) {
            throw new ProductException(ProductErrorCode.CATEGORY_PARENT_NULL);
        }

        return Category.builder()
                .categoryId(categoryId)
                .name(name)
                .parent(parent)
                .createdBy(createdBy)
                .build();
    }


    public void addParent(Category parent) {
        if (parent == null) {
            throw new ProductException(ProductErrorCode.CATEGORY_PARENT_NULL);
        }
        this.parent = parent;
        parent.getChildren().add(this);
        updatePathAndDepth(parent);
    }

    public void updatePathAndDepth(Category parent) {
        if (parent == null) { // null: Root(1) 인 경우
            this.depth = 1;
            this.path = this.categoryId + "/";
        } else {
            this.depth = parent.getDepth() + 1;
            this.path = parent.getPath() + this.categoryId + "/";
        }
    }
}
