package com.friday.commerce.catalog.application.facade;

import com.friday.commerce.catalog.application.dto.product.response.ProductBriefResponse;
import java.util.Collection;
import java.util.Map;

public interface ProductFacade {

    java.util.List<ProductBriefResponse> getProductBriefsByIds(Collection<Long> ids);

    void decreaseStocks(Map<Long, Integer> qtyByProductId);
}
