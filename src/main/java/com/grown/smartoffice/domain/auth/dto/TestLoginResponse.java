package com.grown.smartoffice.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestLoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiresIn;
    private boolean autoCreated;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String position;
        private String department;
    }
}
