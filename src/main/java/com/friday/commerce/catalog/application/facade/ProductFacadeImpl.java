package com.friday.commerce.catalog.application.facade;


import com.friday.commerce.catalog.application.dto.product.response.ProductBriefResponse;
import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import com.friday.commerce.catalog.domain.repository.ProductRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductFacadeImpl implements ProductFacade {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ProductBriefResponse> getProductBriefsByIds(Collection<Long> ids) {
        return productRepository.findByBriefsByIds(ids);
    }


    @Transactional
    @Override
    public void decreaseStocks(Map<Long, Integer> qtyByProductId) {
        if (qtyByProductId == null || qtyByProductId.isEmpty()) {
            return;
        }

        List<Product> products = productRepository
                .findAllByProductIdInAndDeletedAtIsNull(qtyByProductId.keySet());

        // (옵션) 존재성 검증 — 사이즈가 다르면 누락된 상품이 있는 것
        if (products.size() != qtyByProductId.size()) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        for (Product p : products) {
            int qty = qtyByProductId.get(p.getProductId());
            if (qty > 0) {
                p.decreaseStock(qty); // Product.ensureSku() 통해 1:1 SKU 차감
            }
        }
        // 트랜잭션 커밋 시점에 flush
    }
}
