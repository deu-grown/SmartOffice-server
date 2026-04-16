package com.grown.smartoffice.domain.department.dto;

import com.grown.smartoffice.domain.department.entity.Department;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DepartmentListResponse {

    private Long id;
    private String name;
    private String description;
    private int userCount;
    private LocalDateTime createdAt;

    public static DepartmentListResponse of(Department department, long userCount) {
        return DepartmentListResponse.builder()
                .id(department.getDeptId())
                .name(department.getDeptName())
                .description(department.getDeptDescription())
                .userCount((int) userCount)
                .createdAt(department.getCreatedAt())
                .build();
    }
}
