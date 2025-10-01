package com.friday.commerce.catalog.application.service;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.catalog.application.usecase.CategoryUseCase;
import com.friday.commerce.catalog.domain.entity.Category;
import com.friday.commerce.catalog.domain.exception.ProductErrorCode;
import com.friday.commerce.catalog.domain.exception.ProductException;
import com.friday.commerce.catalog.domain.policy.CategoryProperties;
import com.friday.commerce.catalog.domain.repository.CategoryRepository;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CategoryService implements CategoryUseCase {

    private final Snowflake snowflake;
    private final CategoryProperties categoryProperties;
    private final CategoryRepository categoryRepository;

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    @Override
    public CreateCategoryResponse createCategory(
            CurrentUserInfo info,
            CreateCategoryRequest request
    ) {
        // "남자/상의/바지" → ["남자","상의","바지"]
        final String delimiter = request.delimiterOrDefault();
        final List<String> categories = Arrays.stream(request.path().split(Pattern.quote(delimiter)))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (categories.isEmpty()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NAME_INVALID);
        }

        // 최대 깊이 (요청 오버라이드가 있으면 우선, 하드리밋 이하)
        int effectiveMaxDepth = Optional.ofNullable(request.maxDepthOverride())
                .orElse(categoryProperties.getMaxDepth());
        effectiveMaxDepth = Math.min(effectiveMaxDepth, categoryProperties.getHardLimit());

        Category currentParent = null;
        int startingDepth = 0;

        if (request.startParentId() != null) {
            currentParent = findCategoryById(request.startParentId());
            startingDepth = currentParent.getDepth();
        }

        // 깊이 검증: 시작 깊이 + 생성하려는 단계 수 ≤ 허용치
        if (startingDepth + categories.size() > effectiveMaxDepth) {
            throw new ProductException(ProductErrorCode.CATEGORY_DEPTH_EXCEEDED);
        }

        List<Long> createdIds = new ArrayList<>();
        List<Category> chain = new ArrayList<>(); // ← 루트 → 리프 체인 수집
        Category last = currentParent;

        for (String name : categories) {
            // 카테고리 존재 여부 확인
            Optional<Category> existing = (last == null)
                    ? categoryRepository.findByParentIsNullAndName(name)
                    : categoryRepository.findByParentAndName(last, name);

            if (existing.isPresent()) {
                last = existing.get();
                continue;
            }

            // 카테고리 생성
            Category newCategory = (last == null)
                    ? Category.createRoot(snowflake.nextId(), name, info.userId())
                    : Category.createChild(snowflake.nextId(), name, last, info.userId());

            newCategory = categoryRepository.save(newCategory);
            createdIds.add(newCategory.getCategoryId());
            chain.add(newCategory);
            last = newCategory;
        }

        return CreateCategoryResponse.fromEntities(
                chain,
                createdIds,
                info.userId()
        );
    }
}
