package com.grown.smartoffice.global.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지네이션 응답 래퍼
 * Spring Data의 Page<T>를 직접 노출하지 않고 필요한 필드만 제어
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
