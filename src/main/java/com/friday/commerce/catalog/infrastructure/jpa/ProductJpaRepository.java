package com.friday.commerce.catalog.infrastructure.jpa;

import com.friday.commerce.catalog.application.dto.product.response.GetProductResponse;
import com.friday.commerce.catalog.application.dto.product.response.ProductBriefResponse;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.repository.ProductRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductRepository {

    @Query("""
        select p from Product p
        where p.productId in :ids
          and p.deletedAt is null
    """)
    List<Product> findAllByProductIdInAndDeletedAtIsNull(@Param("ids") Collection<Long> ids);


    @Override
    @Query("""
        select new com.friday.commerce.catalog.application.dto.product.response.ProductBriefResponse(
            p.productId, p.title, s.price, s.stock, cast(p.status as string)
        )
        from Product p
        join p.productSku s
        where p.productId in :ids
          and p.deletedAt is null
    """)
    List<ProductBriefResponse> findByBriefsByIds(@Param("ids") Collection<Long> ids);


    @Override
    @Query(
            value = """
                    SELECT 
                        p.productId AS productId,
                        p.title     AS title,
                        p.status    AS status,
                        (SELECT MIN(s.price) FROM ProductSku s WHERE s.product = p) AS minPrice,
                        (SELECT i.imageUrl FROM ProductImage i 
                         WHERE i.product = p AND i.sortOrder = 0) AS thumbnailUrl
                    FROM Product p
                    WHERE p.deletedAt IS NULL
                      AND (:productName IS NULL
                           OR LOWER(p.title)   LIKE LOWER(CONCAT('%', :productName, '%'))
                           OR LOWER(p.content) LIKE LOWER(CONCAT('%', :productName, '%')))
                      AND (:categoryId IS NULL
                           OR EXISTS (
                                SELECT 1
                                FROM ProductCategory pc
                                WHERE pc.product = p
                                  AND pc.category.categoryId = :categoryId
                                  AND pc.category.deletedAt IS NULL
                           ))
                      AND (
                           (:minPrice IS NULL AND :maxPrice IS NULL)
                           OR EXISTS (
                                SELECT 1
                                FROM ProductSku s2
                                WHERE s2.product = p
                                  AND (:minPrice IS NULL OR s2.price >= :minPrice)
                                  AND (:maxPrice IS NULL OR s2.price <= :maxPrice)
                           )
                      )
                    """,
            countQuery = """
                        SELECT COUNT(p)
                        FROM Product p
                        WHERE p.deletedAt IS NULL
                          AND (:productName IS NULL
                               OR LOWER(p.title)   LIKE LOWER(CONCAT('%', :productName, '%'))
                               OR LOWER(p.content) LIKE LOWER(CONCAT('%', :productName, '%')))
                          AND (:categoryId IS NULL
                               OR EXISTS (
                                    SELECT 1
                                    FROM ProductCategory pc
                                    WHERE pc.product = p
                                      AND pc.category.categoryId = :categoryId
                                      AND pc.category.deletedAt IS NULL
                               ))
                          AND (
                               (:minPrice IS NULL AND :maxPrice IS NULL)
                               OR EXISTS (
                                    SELECT 1
                                    FROM ProductSku s2
                                    WHERE s2.product = p
                                      AND (:minPrice IS NULL OR s2.price >= :minPrice)
                                      AND (:maxPrice IS NULL OR s2.price <= :maxPrice)
                               )
                          )
                    """
    )
    Page<ProductRepository.CardRow> findCardsBySimpleCondition(
            @Param("productName") String productName,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );
}
