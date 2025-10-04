package com.friday.commerce.catalog.application.service;


import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.response.CreateProductResponse;
import com.friday.commerce.catalog.application.usecase.ProductUseCase;
import com.friday.commerce.catalog.domain.entity.Category;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.entity.ProductCategory;
import com.friday.commerce.catalog.domain.entity.ProductImage;
import com.friday.commerce.catalog.domain.entity.ProductSku;
import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import com.friday.commerce.catalog.domain.repository.CategoryRepository;
import com.friday.commerce.catalog.domain.repository.ProductRepository;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService implements ProductUseCase {

    private final Snowflake snowflake;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public CreateProductResponse createProduct(CreateProductRequest request, CurrentUserInfo info) {
        // 1) 카테고리 배치 로드
        List<Long> categoryIds = request.distinctCategoryIds();
        if (categoryIds.isEmpty()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        List<Category> categories = categoryRepository
                .findAllByCategoryIdInAndDeletedAtIsNull(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        // 2) 상품 생성
        Product product = Product.create(snowflake.nextId(), request.getTitle(),
                request.getContent(), info.userId());

        // 3) SKU (V1: 1개)
        ProductSku sku = ProductSku.create(snowflake.nextId(), null, request.getPrice(),
                request.getStock());
        product.addSku(sku);

        // 4) 이미지
        List<CreateProductRequest.ImageView> views = request.imagesView().stream()
                .sorted(Comparator.comparing(
                        v -> v.sortOrder() == null ? Integer.MAX_VALUE : v.sortOrder()))
                .toList();

        int order = 0;
        for (CreateProductRequest.ImageView v : views) {
            int sort = (v.sortOrder() == null) ? order : Math.max(0, v.sortOrder());
            String caption =
                    (v.caption() == null || v.caption().isBlank()) ? null : v.caption().trim();
            ProductImage img = ProductImage.create(snowflake.nextId(), null, v.imageUrl(), caption,
                    sort);
            product.addImage(img);
            order++;
        }

        // 5) 카테고리 링크
        for (Category c : categories) {
            ProductCategory link = ProductCategory.link(snowflake.nextId(), product, c);
            product.addProductCategory(link);
        }

        // 6) 저장
        product = productRepository.save(product);

        // 7) 응답 (이 메서드 내에서 생성한  sku 변수를 사용)
        return CreateProductResponse.of(product, categories, sku, product.getImages());
    }
}
