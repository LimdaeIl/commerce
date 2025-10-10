package com.friday.commerce.catalog.application.dto.product.request;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.catalog.domain.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductRequest(

        @Size(max = 150, message = "상품명: 최대 150자까지 가능합니다.")
        String title,

        String content,

        ProductStatus status,

        @Valid
        @Size(min = 1, message = "카테고리: 최소 1개 이상이어야 합니다.")
        List<@NotNull(message = "카테고리: categoryId는 null일 수 없습니다.") Long> categoryIds,

        @Min(value = 1, message = "상품가격: 1 이상이어야 합니다.")
        Long price,

        @Min(value = 0, message = "상품재고: 0 이상이어야 합니다.")
        Long stock,

        @Valid
        List<Image> images
) {

    @JsonAutoDetect(fieldVisibility = ANY)
    public static final class Image implements ImageView {

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

    public interface ImageView {

        String imageUrl();

        String caption();

        Integer sortOrder();
    }
}