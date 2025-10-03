package com.friday.commerce.catalog.domain.repository;

import com.friday.commerce.catalog.domain.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Optional<Category> findById(Long categoryId);

    Optional<Category> findByParentIsNullAndName(String name);

    Optional<Category> findByParentAndName(Category last, String name);

    Category save(Category newCat);

    List<Category> findAllByDeletedAtIsNullAndDeletedByIsNullOrderByDepthAscNameAsc();

    Optional<Category> findByCategoryIdAndDeletedAtIsNull(Long categoryId);

    boolean existsByParentIsNullAndNameAndDeletedAtIsNull(String newName);

    boolean existsByParentAndNameAndDeletedAtIsNull(Category parent, String newName);
}
