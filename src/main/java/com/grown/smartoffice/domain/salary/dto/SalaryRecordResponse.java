package com.grown.smartoffice.domain.salary.dto;

import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalaryRecordResponse {

    private Long id;
    private Long userId;
    private String userName;
    private int year;
    private int month;
    private int baseSalary;
    private int overtimePay;
    private int totalPay;
    private SalaryStatus status;

    public static SalaryRecordResponse from(SalaryRecord r) {
        return SalaryRecordResponse.builder()
                .id(r.getSalrecId())
                .userId(r.getUser().getUserId())
                .userName(r.getUser().getEmployeeName())
                .year(r.getSalrecYear())
                .month(r.getSalrecMonth())
                .baseSalary(r.getSalrecBaseSalary())
                .overtimePay(r.getOvertimePay())
                .totalPay(r.getTotalPay())
                .status(r.getSalrecStatus())
                .build();
    }
}
