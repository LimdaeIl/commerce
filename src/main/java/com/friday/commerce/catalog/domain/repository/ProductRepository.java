package com.friday.commerce.catalog.domain.repository;

import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.entity.ProductStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findByProductIdAndDeletedAtIsNullAndDeletedByIsNull(Long productId);

    interface CardRow {

        Long getProductId();

        String getTitle();

        ProductStatus getStatus();

        Long getMinPrice();

        String getThumbnailUrl();
    }

    Page<CardRow> findCardsBySimpleCondition(
            String productName,
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    );
}
