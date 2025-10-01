package com.friday.commerce.catalog.application.dto.category.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.catalog.domain.entity.Category;
import java.util.ArrayList;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCategoryResponse(
        List<Item> categories,   // 루트 → 리프 순서의 카테고리 체인 (생성 + 재사용 포함)
        List<Long> createdIds,   // 이번 요청에서 실제 새로 생성된 categoryId 리스트
        boolean anyCreated,      // 새로 생성된 게 하나라도 있었는지
        Long createdBy           // 요청자 ID
) {

    @JsonAutoDetect(fieldVisibility = ANY) // private라도 직렬화 가능
    private record Item(
            Long categoryId,
            String name,
            String path,
            Integer depth
    ) { }

    /** 체인 + 생성 ID 집합을 받아 응답 DTO로 변환하는 팩토리 */
    public static CreateCategoryResponse fromEntities(
            List<Category> chain,   // 루트 → 리프
            List<Long> createdIds,  // null 가능
            Long createdBy
    ) {
        List<Item> items = new ArrayList<>(chain.size());
        for (Category category : chain) {
            items.add(new Item(
                    category.getCategoryId(),
                    category.getName(),
                    category.getPath(),
                    category.getDepth()
            ));
        }
        boolean anyCreated = createdIds != null && !createdIds.isEmpty();
        // createdIds는 그대로 노출(소비자가 필요 시 per-item created를 자체 계산)
        return new CreateCategoryResponse(
                List.copyOf(items),
                createdIds == null ? List.of() : List.copyOf(createdIds),
                anyCreated,
                createdBy
        );
    }
}
