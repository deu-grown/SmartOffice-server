package com.grown.smartoffice.domain.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DepartmentUpdateRequest {

    @NotBlank(message = "부서명은 필수입니다.")
    @Size(max = 100, message = "부서명은 100자 이하여야 합니다.")
    private String name;

    @Size(max = 255, message = "부서 설명은 255자 이하여야 합니다.")
    private String description;
}
