package com.friday.commerce.catalog.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
    private Category parent;     // Adjacency List: 부모 카테고리

    // Adjacency List: 자식 카테고리 목록
    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    // Materialized Path: 빠른 하위 카테고리 조회를 위한 경로
    // 예: 상위(1) > 중위(15) > 하위(23) -> "1/15/23/"
    // 인덱스 필수!
    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int depth; // 편의성을 위한 Depth

    @Builder(access = AccessLevel.PRIVATE)
    private Category(
            Long categoryId,
            String name,
            Category parent,
            String path,
            int depth
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.parent = parent;
        this.path = path;
        this.depth = depth;
    }

    public static Category create(
            Long categoryId,
            String name,
            Category parent,
            String path,
            int depth
    ) {
        return Category.builder()
                .categoryId(categoryId)
                .name(name)
                .parent(parent)
                .path(path)
                .depth(depth)
                .build();
    }


    public void addParent(Category parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    public void updatePathAndDepth(Category parent) {
        if (parent == null) { // null: 최상위 카테고리
            this.depth = 1;
            this.path = this.categoryId + "/";
        } else {
            this.depth = parent.getDepth() + 1;
            this.path = parent.getPath() + this.categoryId + "/";
        }
    }
}
