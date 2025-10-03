package com.friday.commerce.catalog.application.service;

import com.friday.commerce.catalog.application.dto.category.request.CreateCategoryRequest;
import com.friday.commerce.catalog.application.dto.category.request.UpdateCategoryNameRequest;
import com.friday.commerce.catalog.application.dto.category.response.CreateCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.DeleteCategoryResponse;
import com.friday.commerce.catalog.application.dto.category.response.GetAllCategoriesResponse;
import com.friday.commerce.catalog.application.dto.category.response.UpdateCategoryNameResponse;
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
import java.util.Objects;
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

    private Category mustGetActive(Long categoryId) {
        return categoryRepository.findByCategoryIdAndDeletedAtIsNull(categoryId)
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
            currentParent = mustGetActive(request.startParentId());
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
                    ? categoryRepository.findByParentIsNullAndNameAndDeletedAtIsNull(name)
                    : categoryRepository.findByParentAndNameAndDeletedAtIsNull(last, name);

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

    @Transactional
    @Override
    public UpdateCategoryNameResponse updateName(
            Long categoryId,
            UpdateCategoryNameRequest request,
            CurrentUserInfo info
    ) {
        Category category = mustGetActive(categoryId);

        String newName = request.name();
        if (newName == null || newName.isBlank()) {
            throw new ProductException(ProductErrorCode.CATEGORY_NAME_INVALID);
        }

        // 같은 이름이면 조용히 성공(멱등)
        if (Objects.equals(category.getName(), newName)) {
            return toNameResponse(category);
        }

        // 형제 중복 체크
        boolean duplicated = (category.getParent() == null)
                ? categoryRepository.existsByParentIsNullAndNameAndDeletedAtIsNull(newName)
                : categoryRepository.existsByParentAndNameAndDeletedAtIsNull(category.getParent(),
                        newName);

        if (duplicated) {
            throw new ProductException(ProductErrorCode.CATEGORY_NAME_DUPLICATED);
        }

        // 변경
        category.rename(newName, info.userId());
        // dirty checking 으로 flush

        return toNameResponse(category);
    }

    @Transactional
    @Override
    public DeleteCategoryResponse deleteCategory(Long categoryId, CurrentUserInfo info) {
        // 1) 활성 카테고리만 대상 (이미 삭제된 것은 404로 본다)
        Category category = mustGetActive(categoryId);

        // 2) 활성 자식 존재하면 삭제 불가
        long childCount = categoryRepository.countByParentAndDeletedAtIsNull(category);
        if (childCount > 0) {
            throw new ProductException(ProductErrorCode.CATEGORY_HAS_CHILDREN);
        }

        // 3) 소프트 삭제
        category.softDelete(info.userId());
        // JPA dirty checking 으로 반영됨

        // 4) 응답: 표준 필드 그대로
        return DeleteCategoryResponse.of(category);
    }

    private UpdateCategoryNameResponse toNameResponse(Category category) {
        Long parentId = (category.getParent() == null) ? null : category.getParent().getCategoryId();
        return new UpdateCategoryNameResponse(
                category.getCategoryId(),
                category.getName(),
                parentId,
                category.getPath(),
                category.getDepth()
        );
    }
}
