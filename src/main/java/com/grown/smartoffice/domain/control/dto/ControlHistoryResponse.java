package com.grown.smartoffice.domain.control.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ControlHistoryResponse {
    private Map<String, Object> searchQuery;
    private int totalCount;
    private List<ControlHistoryItem> controlList;
}
