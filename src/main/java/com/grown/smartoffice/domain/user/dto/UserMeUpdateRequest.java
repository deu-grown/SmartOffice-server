package com.grown.smartoffice.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserMeUpdateRequest {

    @Size(max = 20, message = "연락처는 20자 이하여야 합니다.")
    private String phone;

    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    private String password;

    @Size(min = 8, max = 100, message = "현재 비밀번호는 8자 이상 100자 이하여야 합니다.")
    private String currentPassword;
}
