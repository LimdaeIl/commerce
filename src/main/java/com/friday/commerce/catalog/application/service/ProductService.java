package com.friday.commerce.catalog.application.service;


import com.friday.commerce.catalog.application.dto.product.request.CreateProductRequest;
import com.friday.commerce.catalog.application.dto.product.request.DecreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.request.IncreaseStockRequest;
import com.friday.commerce.catalog.application.dto.product.request.UpdateProductRequest;
import com.friday.commerce.catalog.application.dto.product.response.GetAllProductsResponse;
import com.friday.commerce.catalog.application.dto.product.response.GetProductResponse;
import com.friday.commerce.catalog.application.usecase.ProductUseCase;
import com.friday.commerce.catalog.domain.entity.Category;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.entity.ProductCategory;
import com.friday.commerce.catalog.domain.entity.ProductImage;
import com.friday.commerce.catalog.domain.entity.ProductSku;
import com.friday.commerce.catalog.domain.entity.ProductStatus;
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
        Product product = Product.create(
                snowflake.nextId(),
                request.getTitle(),
                request.getContent(),
                info.userId()
        );

        // 3) SKU (1:1) — create가 양방향(setSku)까지 책임지도록!
        ProductSku sku = ProductSku.create(
                snowflake.nextId(),
                request.getPrice(),
                request.getStock()
        );
        product.setSku(sku);

        // 4) 이미지
        List<CreateProductRequest.ImageView> views = request.imagesView().stream()
                .sorted(Comparator.comparing(
                        v -> v.sortOrder() != null ? v.sortOrder() : Integer.MAX_VALUE))
                .toList();

        for (int i = 0; i < views.size(); i++) {
            CreateProductRequest.ImageView v = views.get(i);
            int sort = v.sortOrder() != null ? Math.max(0, v.sortOrder()) : i;
            String caption =
                    (v.caption() == null || v.caption().isBlank()) ? null : v.caption().trim();
            ProductImage img = ProductImage.create(
                    snowflake.nextId(), null, v.imageUrl(), caption, sort
            );
            product.addImage(img);
        }

        // 5) 카테고리 링크
        for (Category c : categories) {
            ProductCategory link = ProductCategory.link(snowflake.nextId(), product, c);
            product.addProductCategory(link);
        }

        // 6) 저장
        product = productRepository.save(product); // cascade로 sku, images, links 저장

        // 7) 응답
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
                        cardRow.getStatus(),
                        cardRow.getMinPrice(),      // 1:1이어도 그대로 사용 가능(= 단일 가격)
                        cardRow.getThumbnailUrl()
                )
        );
        return PageResponse.from(mapped);
    }

    @Transactional
    @Override
    public GetProductResponse increaseStock(Long productId, IncreaseStockRequest request) {
        Product product = findProductById(productId);
        product.increaseStock(request.quantity()); // ensureSku() 내부에서 검증
        return GetProductResponse.of(product);
    }


    @Transactional
    @Override
    public GetProductResponse decreaseStock(Long productId, DecreaseStockRequest request) {
        Product product = findProductById(productId);
        product.decreaseStock(request.quantity());
        return GetProductResponse.of(product);
    }

    @Transactional
    @Override
    public void delete(Long productId, CurrentUserInfo info) {
        Product product = findProductById(productId);
        product.productStatusArchived(info.userId());
        product.softDelete(info.userId());
    }

    @Transactional(readOnly = true)
    @Override
    public GetProductResponse getProduct(Long productId) {
        Product product = findProductById(productId);
        return GetProductResponse.of(product);
    }

    @Transactional
    @Override
    public GetProductResponse statusDraft(Long productId, CurrentUserInfo info) {
        Product product = findProductById(productId);
        product.draft(info.userId());

        return GetProductResponse.of(product);
    }

    @Transactional
    @Override
    public GetProductResponse statusPublished(Long productId, CurrentUserInfo info) {
        Product product = findProductById(productId);
        product.publish(info.userId());

        return GetProductResponse.of(product);
    }

    @Transactional
    @Override
    public GetProductResponse statusArchived(Long productId, CurrentUserInfo info) {
        Product product = findProductById(productId);
        product.archive(info.userId());

        return GetProductResponse.of(product);
    }

    @Transactional
    @Override
    public GetProductResponse updateProduct(
            Long productId,
            CurrentUserInfo info,
            UpdateProductRequest req
    ) {
        Product product = findProductById(productId);
        boolean changed = false;

        // 1) 본문(제목/내용)
        changed |= applyTitleAndContent(product, info.userId(), req.title(), req.content());

        // 2) 상태
        changed |= applyStatus(product, info.userId(), req.status());

        // 3) 카테고리 전체 교체
        changed |= applyCategories(product, info.userId(), req.categoryIds());

        // 4) SKU(가격/재고)
        changed |= applySku(product, info.userId(), req.price(), req.stock());

        // 5) 이미지 전체 교체
        changed |= applyImages(product, info.userId(), req.images());

        // 변경 없더라도 최신 상태 반환(클라 일관성)
        return GetProductResponse.of(product);
    }

    private boolean applyTitleAndContent(Product product, Long userId, String title,
            String content) {
        boolean changed = false;
        if (title != null) {
            product.updateTitle(title, userId); // 내부에서 trim/blank/len 검증 + 감사 갱신
            changed = true;
        }
        if (content != null) {
            product.updateContent(content, userId); // 내부에서 trim/blank 검증 + 감사 갱신
            changed = true;
        }
        return changed;
    }

    private boolean applyStatus(Product product, Long userId, ProductStatus status) {
        if (status == null) {
            return false;
        }
        if (product.getStatus() == status) {
            return false; // 동일 상태면 예외 대신 미변경 처리
        }
        product.changeStatus(status, userId); // 내부에서 상태 전이 검증 + 감사 갱신
        return true;
    }

    private boolean applyCategories(Product product, Long userId, List<Long> categoryIds) {
        if (categoryIds == null) {
            return false; // 미변경
        }

        // 비즈니스 정책: 최소 1개 필수 (허용하지 않으면 예외)
        if (categoryIds.isEmpty()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        // 중복 제거 + 입력 순서 유지
        java.util.LinkedHashSet<Long> distinct = new java.util.LinkedHashSet<>(categoryIds);
        List<Long> ids = new java.util.ArrayList<>(distinct);

        List<Category> categories = categoryRepository.findAllByCategoryIdInAndDeletedAtIsNull(ids);
        if (categories.size() != ids.size()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        // 링크 재구성
        List<ProductCategory> links = new java.util.ArrayList<>(categories.size());
        for (Category c : categories) {
            links.add(ProductCategory.link(snowflake.nextId(), product, c));
        }

        // 변경 감지: 기존과 동일하면 스킵(선택) — 비용 대비 효과 고려
        // 간단히 전체 교체 후 touch
        product.replaceCategories(links);
        product.touch(userId);
        return true;
    }

    private boolean applySku(Product product, Long userId, Long price, Long stock) {
        if (price == null && stock == null) {
            return false; // 미변경
        }

        ProductSku current = product.getProductSku();
        if (current == null) {
            throw new ProductException(ProductErrorCode.SKU_NOT_FOUND);
        }

        long newPrice = (price != null) ? price : current.getPrice();
        long newStock = (stock != null) ? stock : current.getStock();

        boolean different = (newPrice != current.getPrice()) || (newStock != current.getStock());
        if (!different) {
            return false;
        }

        ProductSku newSku = ProductSku.create(snowflake.nextId(), newPrice, newStock);
        product.setSku(newSku);      // 양방향 / orphanRemoval 처리
        product.touch(userId);
        return true;
    }

    private boolean applyImages(Product product, Long userId,
            List<UpdateProductRequest.Image> images) {
        if (images == null) {
            return false; // 미변경
        }

        // []: 모두 삭제, 값 있으면 교체
        List<ProductImage> newImages = new java.util.ArrayList<>(images.size());
        for (int i = 0; i < images.size(); i++) {
            var v = images.get(i);
            int sort = v.sortOrder() != null ? Math.max(0, v.sortOrder()) : i; // 음수 방지
            String caption =
                    (v.caption() == null || v.caption().isBlank()) ? null : v.caption().trim();
            ProductImage img = ProductImage.create(snowflake.nextId(), null, v.imageUrl(), caption,
                    sort);
            newImages.add(img);
        }
        product.replaceImages(newImages);
        product.touch(userId);
        return true;
    }
}
