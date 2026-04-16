package com.grown.smartoffice.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "사번은 필수입니다.")
    @Size(max = 20, message = "사번은 20자 이하여야 합니다.")
    private String employeeNumber;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "권한은 필수입니다.")
    private String role;

    @NotBlank(message = "직급은 필수입니다.")
    @Size(max = 50)
    private String position;

    private Long departmentId;

    @Size(max = 20)
    private String phone;

    private LocalDate hiredAt;
}
