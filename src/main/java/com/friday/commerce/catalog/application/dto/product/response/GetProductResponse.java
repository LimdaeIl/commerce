package com.friday.commerce.catalog.application.dto.product.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.catalog.domain.entity.Category;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.entity.ProductCategory;
import com.friday.commerce.catalog.domain.entity.ProductImage;
import com.friday.commerce.catalog.domain.entity.ProductSku;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetProductResponse(
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

    public static GetProductResponse of(Product product) {
        // categories
        List<CategoryItem> categoryItems = product.getProductCategories() == null ? List.of() :
                product.getProductCategories().stream()
                        .map(ProductCategory::getCategory)                    // ProductCategory -> Category
                        .filter(java.util.Objects::nonNull)
                        .sorted(Comparator.comparing(Category::getDepth)
                                .thenComparing(Category::getPath))
                        .map(category -> CategoryItem.builder()
                                .categoryId(category.getCategoryId())
                                .name(category.getName())
                                .path(category.getPath())
                                .depth(category.getDepth())
                                .build())
                        .toList();

        // sku (최저가 선택, 없으면 null)
        SkuItem skuItem = null;
        if (product.getProductSkus() != null && !product.getProductSkus().isEmpty()) {
            ProductSku selected = product.getProductSkus().stream()
                    .min(Comparator.comparing(ProductSku::getPrice))
                    .orElse(null);
            skuItem = SkuItem.builder()
                    .productSkuId(selected.getProductSkuId())
                    .price(selected.getPrice())
                    .stock(selected.getStock())
                    .build();
        }

        // images (sortOrder 오름차순, null은 뒤로)
        List<ImageItem> imageItems = product.getImages() == null ? List.of() :
                product.getImages().stream()
                        .sorted(Comparator.comparing(
                                ProductImage::getSortOrder,
                                java.util.Comparator.nullsLast(Integer::compareTo)))
                        .map(img -> ImageItem.builder()
                                .productImageId(img.getProductImageId())
                                .imageUrl(img.getImageUrl())
                                .caption(img.getCaption())
                                .sortOrder(img.getSortOrder())
                                .build())
                        .toList();

        return GetProductResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .content(product.getContent())
                .status(product.getStatus().name())
                .categories(categoryItems)
                .sku(skuItem)
                .images(imageItems)
                .build();
    }

    public static GetProductResponse of(
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

        return GetProductResponse.builder()
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
