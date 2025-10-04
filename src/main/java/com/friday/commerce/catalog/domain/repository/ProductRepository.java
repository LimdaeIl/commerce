package com.friday.commerce.catalog.domain.repository;

import com.friday.commerce.catalog.domain.entity.Product;

public interface ProductRepository {

    Product save(Product product);
}
