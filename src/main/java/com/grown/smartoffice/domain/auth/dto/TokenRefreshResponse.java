package com.grown.smartoffice.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponse {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
}
