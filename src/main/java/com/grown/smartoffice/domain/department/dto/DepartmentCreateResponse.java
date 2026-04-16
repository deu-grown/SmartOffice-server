package com.grown.smartoffice.domain.department.dto;

import com.grown.smartoffice.domain.department.entity.Department;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DepartmentCreateResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public static DepartmentCreateResponse from(Department department) {
        return DepartmentCreateResponse.builder()
                .id(department.getDeptId())
                .name(department.getDeptName())
                .description(department.getDeptDescription())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
