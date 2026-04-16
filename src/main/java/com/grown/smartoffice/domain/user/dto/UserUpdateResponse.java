package com.grown.smartoffice.domain.user.dto;

import com.grown.smartoffice.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserUpdateResponse {

    private Long id;
    private String name;
    private String role;
    private String position;
    private String department;
    private String phone;
    private LocalDateTime updatedAt;

    public static UserUpdateResponse from(User user) {
        String deptName = (user.getDepartment() != null) ? user.getDepartment().getDeptName() : null;
        return UserUpdateResponse.builder()
                .id(user.getUserId())
                .name(user.getEmployeeName())
                .role(user.getRole().name())
                .position(user.getPosition())
                .department(deptName)
                .phone(user.getPhone())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
