package com.grown.smartoffice.domain.accesslog.dto;

import com.grown.smartoffice.global.common.PageResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserAccessLogListResponse {
    private Long userId;
    private String userName;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private List<AccessLogResponse> logs;

    public static UserAccessLogListResponse of(Long userId, String userName, PageResponse<AccessLogResponse> pageResponse) {
        return UserAccessLogListResponse.builder()
                .userId(userId)
                .userName(userName)
                .totalElements(pageResponse.totalElements())
                .totalPages(pageResponse.totalPages())
                .currentPage(pageResponse.page())
                .logs(pageResponse.content())
                .build();
    }
}
