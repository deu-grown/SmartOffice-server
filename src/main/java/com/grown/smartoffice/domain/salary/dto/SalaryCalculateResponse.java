package com.grown.smartoffice.domain.salary.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SalaryCalculateResponse {

    private int totalCount;
    private int successCount;
    private int skipCount;
    private List<SalaryRecordResponse> records;
}
