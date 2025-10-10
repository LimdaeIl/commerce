package com.friday.commerce.order.infrastructure.external;

import com.friday.commerce.catalog.application.facade.ProductFacade;
import com.friday.commerce.order.application.port.out.CatalogPort;
import com.friday.commerce.order.domain.entity.ProductBrief;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
class CatalogAdapter implements CatalogPort {

    private final ProductFacade productFacade;

    @Override
    public Map<Long, ProductBrief> getProductsByIds(Collection<Long> ids) {
        return productFacade.getProductBriefsByIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.friday.commerce.catalog.application.dto.product.response.ProductBriefResponse::productId,
                        r -> new ProductBrief(r.productId(), r.productTitle(), r.price(), r.stock())
                ));
    }

    @Override
    public void decreaseStocks(Map<Long, Integer> requestedQtyByProduct) {
        productFacade.decreaseStocks(requestedQtyByProduct);
    }
}
