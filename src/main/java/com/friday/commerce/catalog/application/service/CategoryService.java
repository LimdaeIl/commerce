package com.friday.commerce.catalog.application.service;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.GetAllCategoriesResponse;
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
        final List<String> tokens = Arrays.stream(request.path().split(Pattern.quote(delimiter)))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (tokens.isEmpty()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NAME_INVALID);
        }

        // 효과적 최대 깊이 계산 (요청 오버라이드 우선, 하드리밋 이하)
        int effectiveMaxDepth = Optional.ofNullable(request.maxDepthOverride())
                .orElse(categoryProperties.getMaxDepth());
        effectiveMaxDepth = Math.min(effectiveMaxDepth, categoryProperties.getHardLimit());

        Category currentParent = null;
        int startingDepth = 0;

        if (request.startParentId() != null) {
            currentParent = findCategoryById(request.startParentId());
            startingDepth = currentParent.getDepth();
        }

        // 깊이 검증: 시작 깊이 + 생성 단계 수 ≤ 허용치
        if (startingDepth + tokens.size() > effectiveMaxDepth) {
            throw new ProductException(ProductErrorCode.CATEGORY_DEPTH_EXCEEDED);
        }

        List<Long> createdIds = new ArrayList<>();
        List<Category> chain = new ArrayList<>(); // 루트 → 리프(재사용 + 생성 포함)
        Category last = currentParent;

        for (String name : tokens) {
            // 1) 존재 여부 확인
            Optional<Category> existing = (last == null)
                    ? categoryRepository.findByParentIsNullAndName(name)
                    : categoryRepository.findByParentAndName(last, name);

            if (existing.isPresent()) {
                last = existing.get();
                chain.add(last);   // 재사용된 노드도 체인에 포함
                continue;
            }

            // 2) 없으면 생성
            Category newCategory = (last == null)
                    ? Category.createRoot(snowflake.nextId(), name, info.userId())
                    : Category.createChild(snowflake.nextId(), name, last, info.userId());

            newCategory = categoryRepository.save(newCategory);
            createdIds.add(newCategory.getCategoryId());
            chain.add(newCategory);
            last = newCategory;
        }

        return CreateCategoryResponse.fromEntities(
                chain,              // 루트 → 리프 전체 체인
                createdIds,         // 생성된 categoryId만
                info.userId()       // 요청자
        );
    }

    @Transactional(readOnly = true)
    @Override
    public GetAllCategoriesResponse getAllCategories(String format, Integer maxDepth) {
        // v1: 전체를 한 번에 읽고 메모리에서 조립 (N+1 없음)
        List<Category> all = categoryRepository
                .findAllByDeletedAtIsNullAndDeletedByIsNullOrderByDepthAscNameAsc();

        if ("flat".equalsIgnoreCase(format)) {
            // 플랫 + childrenCount(직계) 포함
            return GetAllCategoriesResponse.asFlatFromEntities(all);
        }
        // 트리 + childrenCount(=children.size()) 포함, maxDepth 적용 가능
        return GetAllCategoriesResponse.asTreeFromEntities(all, maxDepth);
    }
}
