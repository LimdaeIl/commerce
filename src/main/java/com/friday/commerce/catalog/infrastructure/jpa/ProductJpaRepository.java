package com.friday.commerce.catalog.infrastructure.jpa;

import com.friday.commerce.catalog.domain.entity.Product;
import com.friday.commerce.catalog.domain.repository.ProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductRepository {

}
