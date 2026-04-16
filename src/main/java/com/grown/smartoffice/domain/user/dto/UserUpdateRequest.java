package com.grown.smartoffice.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    private String role;

    @Size(max = 50)
    private String position;

    private Long departmentId;

    @Size(max = 20)
    private String phone;

    private LocalDate hiredAt;
}
