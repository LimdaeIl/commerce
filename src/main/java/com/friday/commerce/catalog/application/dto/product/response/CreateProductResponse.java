package com.friday.commerce.catalog.application.dto.product.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.catalog.domain.entity.Category;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.entity.ProductImage;
import com.friday.commerce.catalog.domain.entity.ProductSku;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateProductResponse(
        Long productId,
        String title,
        String content,
        String status,
        List<CategoryItem> categories,
        SkuItem sku,
        List<ImageItem> images
) {

    @Builder(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = ANY)
    public record CategoryItem(
            Long categoryId,
            String name,
            String path,
            Integer depth
    ) {

    }

    @Builder(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = ANY)
    public record SkuItem(
            Long productSkuId,
            Long price,
            Long stock
    ) {

    }


    @Builder(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = ANY)
    public record ImageItem(
            Long productImageId,
            String imageUrl,
            String caption,
            Integer sortOrder
    ) {

    }

    public static CreateProductResponse of(
            Product product,
            List<Category> categories,
            ProductSku productSku,
            List<ProductImage> productImages
    ) {
        List<CategoryItem> categoriesItems = categories.stream()
                .sorted(Comparator.comparing(Category::getDepth)
                        .thenComparing(Category::getPath))
                .map(category -> CategoryItem.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .path(category.getPath())
                        .depth(category.getDepth())
                        .build())
                .toList();

        SkuItem skuItem = SkuItem.builder()
                .productSkuId(productSku.getProductSkuId())
                .price(productSku.getPrice())
                .stock(productSku.getStock())
                .build();

        List<ImageItem> imageItems = new ArrayList<>(productImages.size());
        for (ProductImage productImage : productImages) {
            imageItems.add(
                    ImageItem.builder()
                    .productImageId(productImage.getProductImageId())
                    .imageUrl(productImage.getImageUrl())
                    .caption(productImage.getCaption())
                    .sortOrder(productImage.getSortOrder())
                    .build()
            );
        }

        return CreateProductResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .content(product.getContent())
                .status(product.getStatus().name())
                .categories(categoriesItems)
                .sku(skuItem)
                .images(imageItems)
                .build();
    }


}
