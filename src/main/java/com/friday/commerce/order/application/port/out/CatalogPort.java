package com.friday.commerce.order.application.port.out;

import com.friday.commerce.order.domain.entity.ProductBrief;
import java.util.Collection;
import java.util.Map;

public interface CatalogPort {

    Map<Long, ProductBrief> getProductsByIds(Collection<Long> ids);

    void decreaseStocks(Map<Long, Integer> requestedQtyByProduct);
}
