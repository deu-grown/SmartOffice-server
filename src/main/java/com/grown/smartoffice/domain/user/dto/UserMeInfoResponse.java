package com.grown.smartoffice.domain.user.dto;

import com.grown.smartoffice.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserMeInfoResponse {

    private Long id;
    private String employeeNumber;
    private String name;
    private String email;
    private String role;
    private String position;
    private String department;
    private String phone;
    private String status;
    private LocalDate hiredAt;

    public static UserMeInfoResponse from(User user) {
        String deptName = (user.getDepartment() != null) ? user.getDepartment().getDeptName() : null;
        return UserMeInfoResponse.builder()
                .id(user.getUserId())
                .employeeNumber(user.getEmployeeNumber())
                .name(user.getEmployeeName())
                .email(user.getEmployeeEmail())
                .role(user.getRole().name())
                .position(user.getPosition())
                .department(deptName)
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .hiredAt(user.getHiredAt())
                .build();
    }
}
