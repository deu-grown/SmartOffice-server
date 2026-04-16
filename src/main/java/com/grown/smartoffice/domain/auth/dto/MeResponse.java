package com.grown.smartoffice.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeResponse {

    private Long id;
    private String employeeNumber;
    private String name;
    private String email;
    private String role;
    private String position;
    private String department;
    private String phone;
    private String hiredAt;
    private String status;
}
