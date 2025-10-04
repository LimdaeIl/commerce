package com.friday.commerce.catalog.application.service;


import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.request.DecreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.request.IncreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.response.GetProductResponse;
import com.friday.commerce.catalog.application.dto.product.response.GetAllProductsResponse;
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
import com.friday.commerce.catalog.domain.repository.ProductRepository.CardRow;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.core.web.response.PageResponse;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService implements ProductUseCase {

    private final Snowflake snowflake;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private Product findProductById(Long productId) {
        return productRepository.findByProductIdAndDeletedAtIsNullAndDeletedByIsNull(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    @Override
    public GetProductResponse createProduct(CreateProductRequest request, CurrentUserInfo info) {
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
                        v -> v.sortOrder() != null ? v.sortOrder() : Integer.MAX_VALUE))
                .toList();

        for (CreateProductRequest.ImageView v : views) {
            int sort = v.sortOrder() != null ? Math.max(0, v.sortOrder()) : views.indexOf(v);
            String caption =
                    (v.caption() == null || v.caption().isBlank()) ? null : v.caption().trim();
            ProductImage img = ProductImage.create(snowflake.nextId(), null, v.imageUrl(), caption,
                    sort);
            product.addImage(img);
        }

        // 5) 카테고리 링크
        for (Category c : categories) {
            ProductCategory link = ProductCategory.link(snowflake.nextId(), product, c);
            product.addProductCategory(link);
        }

        // 6) 저장
        product = productRepository.save(product);

        // 7) 응답 (이 메서드 내에서 생성한  sku 변수를 사용)
        return GetProductResponse.of(product, categories, sku, product.getImages());
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<GetAllProductsResponse> getAllProducts(
            String productName,
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            Pageable pageable
    ) {
        Page<CardRow> page = productRepository.findCardsBySimpleCondition(
                productName, categoryId, minPrice, maxPrice, pageable
        );

        Page<GetAllProductsResponse> mapped = page.map(cardRow ->
                new GetAllProductsResponse(
                        cardRow.getProductId(),
                        cardRow.getTitle(),
                        cardRow.getStatus(),    // enum 그대로
                        cardRow.getMinPrice(),
                        cardRow.getThumbnailUrl()
                )
        );
        return PageResponse.from(mapped);
    }

    @Transactional
    @Override
    public GetProductResponse increaseStock(Long productId, Long productSkuId, IncreaseStockRequest request) {
        Product product = findProductById(productId);

        product.increaseStock(productSkuId, request.quantity());

        return GetProductResponse.of(product);
    }


    @Transactional
    @Override
    public GetProductResponse decreaseStock(Long productId, Long productSkuId,
            DecreaseStockRequest request) {
        Product product = findProductById(productId);

        product.decreaseStock(productSkuId, request.quantity());

        return GetProductResponse.of(product);
    }

    @Transactional
    @Override
    public GetProductResponse delete(Long productId, CurrentUserInfo info) {
        Product product = findProductById(productId);

        product.productStatusArchived(info.userId());
        product.softDelete(info.userId());

        return GetProductResponse.of(product);
    }
}
