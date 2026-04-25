package com.grown.smartoffice.domain.accesslog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TagEventResponse {

    private String authResult;
    private String denyReason;
    private Long userId;
    private LocalDateTime taggedAt;
}
