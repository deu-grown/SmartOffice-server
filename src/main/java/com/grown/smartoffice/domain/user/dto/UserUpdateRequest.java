package com.grown.smartoffice.domain.user.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    private String name;

    @Size(max = 20, message = "권한 값이 올바르지 않습니다.")
    private String role;

    @Size(max = 50)
    private String position;

    @Positive(message = "부서 ID는 양수여야 합니다.")
    private Long departmentId;

    @Size(max = 20)
    private String phone;

    private LocalDate hiredAt;
}
