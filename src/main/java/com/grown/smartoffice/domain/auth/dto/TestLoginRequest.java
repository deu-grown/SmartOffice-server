package com.grown.smartoffice.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestLoginRequest {

    private String email;
    private String role;
}
