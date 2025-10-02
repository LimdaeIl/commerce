package com.friday.commerce.catalog.application.dto.category.response;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
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

    /* ===================== v1: 엔티티 전체 로딩 버전 ===================== */

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
        List<Node> rebuilt = sortAndRebuild(roots, maxDepth);
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

    /* ===================== v2 대비: 플랫 로우 인터페이스 버전 ===================== */

    public interface CategoryFlatRow {

        Long categoryId();

        String name();

        Long parentId();

        String path();

        Integer depth();
    }

    public static GetAllCategoriesResponse asTree(List<CategoryFlatRow> rows, Integer maxDepth) {
        int total = rows.size();
        Map<Long, Node> map = new HashMap<>(Math.max(16, total * 2));
        for (var r : rows) {
            map.put(r.categoryId(), new Node(
                    r.categoryId(), r.name(), r.parentId(), r.path(), r.depth(),
                    0, new ArrayList<>()
            ));
        }
        List<Node> roots = new ArrayList<>();
        for (var r : rows) {
            Node me = map.get(r.categoryId());
            if (r.parentId() == null) {
                roots.add(me);
            } else {
                Node p = map.get(r.parentId());
                if (p != null) {
                    p.children().add(me);
                } else {
                    roots.add(me);
                }
            }
        }
        List<Node> rebuilt = sortAndRebuild(roots, maxDepth);
        return new GetAllCategoriesResponse(List.copyOf(rebuilt), null, total, "v1");
    }

    public static GetAllCategoriesResponse asFlat(List<CategoryFlatRow> rows) {
        int total = rows.size();

        Map<Long, Integer> childCnt = new HashMap<>();
        for (var r : rows) {
            if (r.parentId() != null) {
                childCnt.merge(r.parentId(), 1, Integer::sum);
            }
        }

        List<Item> items = new ArrayList<>(total);
        for (var r : rows) {
            int childrenCount = childCnt.getOrDefault(r.categoryId(), 0);
            items.add(new Item(
                    r.categoryId(), r.name(), r.parentId(), r.path(), r.depth(), childrenCount
            ));
        }
        return new GetAllCategoriesResponse(null, List.copyOf(items), total, "v1");
    }

    /* ===================== 내부 유틸 ===================== */

    private static List<Node> sortAndRebuild(List<Node> nodes, Integer maxDepth) {
        // 정렬
        nodes.sort(Comparator.comparing(Node::name).thenComparing(Node::categoryId));

        List<Node> out = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            boolean pruneHere = (maxDepth != null && maxDepth > 0 && n.depth() >= maxDepth);
            List<Node> children = pruneHere ? List.of() : sortAndRebuild(n.children(), maxDepth);
            int childrenCount = children.size();
            out.add(new Node(
                    n.categoryId(), n.name(), n.parentId(), n.path(), n.depth(),
                    childrenCount, children
            ));
        }
        return out;
    }

    // "10/15/23/" → 15 (루트면 null)
    private static Long parentIdFromPath(String path) {

        if (path == null || path.isBlank()) {
            return null;
        }

        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int last = p.lastIndexOf('/');

        if (last < 0) {
            return null; // "10" → 루트
        }

        String parentPart = p.substring(0, last);
        int last2 = parentPart.lastIndexOf('/');
        String parentIdStr = (last2 < 0) ? parentPart : parentPart.substring(last2 + 1);
        return parentIdStr.isBlank() ? null : Long.parseLong(parentIdStr);
    }
}
