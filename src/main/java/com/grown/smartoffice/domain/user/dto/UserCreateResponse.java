package com.grown.smartoffice.domain.user.dto;

import com.grown.smartoffice.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserCreateResponse {

    private Long id;
    private String employeeNumber;
    private String name;
    private String email;
    private String role;
    private String position;
    private String department;
    private String status;
    private LocalDate hiredAt;
    private LocalDateTime createdAt;

    public static UserCreateResponse from(User user) {
        String deptName = (user.getDepartment() != null) ? user.getDepartment().getDeptName() : null;
        return UserCreateResponse.builder()
                .id(user.getUserId())
                .employeeNumber(user.getEmployeeNumber())
                .name(user.getEmployeeName())
                .email(user.getEmployeeEmail())
                .role(user.getRole().name())
                .position(user.getPosition())
                .department(deptName)
                .status(user.getStatus().name())
                .hiredAt(user.getHiredAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
