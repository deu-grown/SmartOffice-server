package com.grown.smartoffice.domain.department.dto;

import com.grown.smartoffice.domain.department.entity.Department;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DepartmentUpdateResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime updatedAt;

    public static DepartmentUpdateResponse from(Department department) {
        return DepartmentUpdateResponse.builder()
                .id(department.getDeptId())
                .name(department.getDeptName())
                .description(department.getDeptDescription())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
