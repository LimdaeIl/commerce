package com.friday.commerce.catalog.application.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryPathUtil {

    /**
     * 경로 문자열에서 부모 ID를 추출합니다.
     * @param path 카테고리 경로 (예: "10/15/23/")
     * @return 부모 ID, 루트인 경우 null
     */
    public static Long extractParentId(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int last = p.lastIndexOf('/');
        if (last < 0) {
            return null;
        }
        String parentPart = p.substring(0, last);
        int last2 = parentPart.lastIndexOf('/');
        String parentIdStr = (last2 < 0) ? parentPart : parentPart.substring(last2 + 1);
        if (parentIdStr.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(parentIdStr);
        } catch (NumberFormatException e) {
            // 로깅 권장
            return null;
        }
    }
}
