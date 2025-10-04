package com.friday.commerce.catalog.application.dto.product.request;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateProductRequest {

    @NotBlank(message = "상품명: 필수입니다.")
    @Size(max = 150, message = "상품명: 최대 150자까지 가능합니다.")
    private String title;

    @NotBlank(message = "상품내용: 필수입니다.")
    private String content;

    @NotNull(message = "카테고리: 최소 1개 이상 지정해야 합니다.")
    @Size(min = 1, message = "카테고리: 최소 1개 이상필요합니다.")
    private List<@NotNull(message = "카테고리: categoryId는 null일 수 없습니다.") Long> categoryIds;

    @NotNull(message = "SKU: 필수입니다.")
    @Valid
    private Sku skus;

    @Valid
    private List<@NotNull(message = "상품 이미지 항목은 null일 수 없습니다.") Image> images;

    public List<Long> distinctCategoryIds() {
        if (categoryIds == null) {
            return List.of();
        }
        Set<Long> set = new LinkedHashSet<>(categoryIds);
        return new ArrayList<>(set);
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonAutoDetect(fieldVisibility = ANY)
    private static class Sku {

        @NotNull(message = "상품가격: 필수입니다.")
        @Min(value = 1, message = "상품가격: 1 이상이어야 합니다.")
        private Long price;

        @NotNull(message = "상품재고: 필수입니다.")
        @Min(value = 0, message = "상품재고: 0 이상이어야 합니다.")
        private Long stock;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @JsonAutoDetect(fieldVisibility = ANY)
    private static class Image implements ImageView {

        @NotBlank(message = "상품 이미지 URL: 필수입니다.")
        @Size(max = 2048, message = "상품 이미지 URL: 최대 2048자까지 가능합니다.")
        private String imageUrl;

        @Size(max = 200, message = "상품 이미지 캡션: 최대 200자까지 가능합니다.")
        private String caption;

        @Min(value = 0, message = "상품 이미지 정렬 순서: 0 이상이어야 합니다.")
        private Integer sortOrder;

        @Override
        public String imageUrl() {
            return imageUrl;
        }

        @Override
        public String caption() {
            return caption;
        }

        @Override
        public Integer sortOrder() {
            return sortOrder;
        }
    }

    public Long getPrice() {
        return skus.price;
    }

    public Long getStock() {
        return skus.stock;
    }


    public interface ImageView {

        String imageUrl();

        String caption();

        Integer sortOrder();
    }

    public List<ImageView> imagesView() {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        // Image가 ImageView를 구현
        return List.copyOf(images);
    }
}
