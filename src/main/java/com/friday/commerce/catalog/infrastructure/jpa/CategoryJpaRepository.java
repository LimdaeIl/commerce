package com.friday.commerce.catalog.infrastructure.jpa;

import com.friday.commerce.catalog.application.dto.category.response.CategoryFlatRow;
import com.friday.commerce.catalog.domain.entity.Category;
import com.friday.commerce.catalog.domain.repository.CategoryRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long>, CategoryRepository {

}
