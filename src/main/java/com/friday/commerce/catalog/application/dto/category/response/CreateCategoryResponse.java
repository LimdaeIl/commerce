package com.friday.commerce.catalog.application.dto.category.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.catalog.application.util.CategoryPathUtil;
import com.friday.commerce.catalog.domain.entity.Category;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCategoryResponse(
        List<Item> categories,           // 루트 → 리프 순서(생성 + 재사용 포함)
        List<Long> createdCategoryIds,   // 이번 요청에서 새로 생성된 categoryId 목록
        boolean anyCreated,              // 하나라도 생성되었는지
        Long createdBy                   // 요청자 ID
) {

    @JsonAutoDetect(fieldVisibility = ANY)
    private record Item(
            Long categoryId,
            String name,
            Long parentId,
            String path,
            Integer depth
    ) {

    }

    public static CreateCategoryResponse fromEntities(
            List<Category> chain,       // 루트 → 리프
            List<Long> createdIds,      // null 가능
            Long requesterId
    ) {
        List<Item> items = new ArrayList<>(chain.size());
        for (Category c : chain) {
            items.add(new Item(
                    c.getCategoryId(),
                    c.getName(),
                    parentIdFromPath(c.getPath()),
                    c.getPath(),
                    c.getDepth()
            ));
        }
        boolean anyCreated = createdIds != null && !createdIds.isEmpty();
        return new CreateCategoryResponse(
                List.copyOf(items),
                createdIds == null ? List.of() : List.copyOf(createdIds),
                anyCreated,
                requesterId
        );
    }

    private static Long parentIdFromPath(String path) {
        return CategoryPathUtil.extractParentId(path);
    }
}
