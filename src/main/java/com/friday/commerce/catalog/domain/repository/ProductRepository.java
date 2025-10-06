package com.friday.commerce.catalog.domain.repository;

import com.friday.commerce.catalog.application.dto.product.response.GetProductResponse;
import com.friday.commerce.catalog.application.dto.product.response.ProductBriefResponse;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.entity.ProductStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findByProductIdAndDeletedAtIsNullAndDeletedByIsNull(Long productId);

    List<ProductBriefResponse> findByBriefsByIds(Collection<Long> ids);

    List<Product> findAllByProductIdInAndDeletedAtIsNull(Set<Long> longId);

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
