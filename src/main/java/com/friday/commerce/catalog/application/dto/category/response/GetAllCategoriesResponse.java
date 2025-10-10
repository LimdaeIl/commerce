package com.friday.commerce.catalog.application.dto.category.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.friday.commerce.catalog.application.util.CategoryPathUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetAllCategoriesResponse(
        List<Node> tree,   // format=tree일 때 사용 (루트 → children)
        List<Item> flat,   // format=flat일 때 사용
        int totalCount,
        String version     // e.g. "v1"
) {

    @JsonAutoDetect(fieldVisibility = ANY)
    public record Node(
            Long categoryId,
            String name,
            Long parentId,
            String path,
            Integer depth,
            Integer childrenCount,
            List<Node> children
    ) {

    }

    @JsonAutoDetect(fieldVisibility = ANY)
    public record Item(
            Long categoryId,
            String name,
            Long parentId,
            String path,
            Integer depth,
            Integer childrenCount
    ) {

    }

    public static GetAllCategoriesResponse asTreeFromEntities(
            List<com.friday.commerce.catalog.domain.entity.Category> entities,
            Integer maxDepth
    ) {
        int total = entities.size();

        // 1) categoryId -> 임시 Node( children만 채우는 용 ) 맵
        Map<Long, Node> map = new HashMap<>(Math.max(16, total * 2));
        for (var c : entities) {
            map.put(c.getCategoryId(), new Node(
                    c.getCategoryId(), c.getName(), parentIdFromPath(c.getPath()),
                    c.getPath(), c.getDepth(),
                    0, // 후에 재빌드에서 실제 childrenCount 채움
                    new ArrayList<>()
            ));
        }

        // 2) 부모-자식 연결
        List<Node> roots = new ArrayList<>();
        for (var c : entities) {
            Node me = map.get(c.getCategoryId());
            Long pid = parentIdFromPath(c.getPath());
            if (pid == null) {
                roots.add(me);
            } else {
                Node p = map.get(pid);
                if (p != null) {
                    p.children().add(me);
                } else {
                    roots.add(me); // 방어
                }
            }
        }

        // 3) 정렬 + (깊이 제한 적용 + childrenCount 채운) 새 트리로 재빌드
        List<Node> rebuilt = sortAndRebuild(roots, normalizeMaxDepth(maxDepth));
        return new GetAllCategoriesResponse(List.copyOf(rebuilt), null, total, "v1");
    }

    public static GetAllCategoriesResponse asFlatFromEntities(
            List<com.friday.commerce.catalog.domain.entity.Category> entities
    ) {
        int total = entities.size();

        // parentId별 자식 수 집계
        Map<Long, Integer> childCnt = new HashMap<>();
        for (var c : entities) {
            Long pid = parentIdFromPath(c.getPath());
            if (pid != null) {
                childCnt.merge(pid, 1, Integer::sum);
            }
        }

        List<Item> items = new ArrayList<>(total);
        for (var c : entities) {
            int childrenCount = childCnt.getOrDefault(c.getCategoryId(), 0);
            items.add(new Item(
                    c.getCategoryId(), c.getName(), parentIdFromPath(c.getPath()),
                    c.getPath(), c.getDepth(), childrenCount
            ));
        }
        return new GetAllCategoriesResponse(null, List.copyOf(items), total, "v1");
    }


    /**
     * maxDepth 해석:
     * - depth는 1-base (루트=1) 입니다.
     * - maxDepth == null: 깊이 제한 없음(전체)
     * - maxDepth == 1: 루트만, 자식 모두 비움
     * - maxDepth == 2: 루트 + 즉시 하위
     * - maxDepth <= 0: 1로 보정(루트만)
     */
    private static List<Node> sortAndRebuild(List<Node> nodes, int maxDepth) {
        nodes.sort(Comparator.comparing(Node::name).thenComparing(Node::categoryId));

        List<Node> out = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            // depth(1-base)가 maxDepth 이상이면 자식 제거
            boolean pruneHere = n.depth() >= maxDepth;
            List<Node> children = pruneHere ? List.of() : sortAndRebuild(n.children(), maxDepth);
            int childrenCount = children.size();
            out.add(new Node(
                    n.categoryId(), n.name(), n.parentId(), n.path(), n.depth(),
                    childrenCount, children
            ));
        }
        return out;
    }

    private static int normalizeMaxDepth(Integer maxDepth) {
        if (maxDepth == null) return Integer.MAX_VALUE; // 무제한
        return Math.max(1, maxDepth);                   // 0, 음수 → 1
    }

    private static Long parentIdFromPath(String path) {
        return CategoryPathUtil.extractParentId(path);
    }

}
