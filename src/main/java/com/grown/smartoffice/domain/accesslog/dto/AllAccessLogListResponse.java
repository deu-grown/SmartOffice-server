package com.grown.smartoffice.domain.accesslog.dto;

import com.grown.smartoffice.global.common.PageResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AllAccessLogListResponse {
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private List<AccessLogResponse> logs;

    public static AllAccessLogListResponse from(PageResponse<AccessLogResponse> pageResponse) {
        return AllAccessLogListResponse.builder()
                .totalElements(pageResponse.totalElements())
                .totalPages(pageResponse.totalPages())
                .currentPage(pageResponse.page())
                .logs(pageResponse.content())
                .build();
    }
}
