package com.grown.smartoffice.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserMeUpdateRequest {

    @Size(max = 20, message = "연락처는 20자 이하여야 합니다.")
    private String phone;

    private String password;

    private String currentPassword;
}
