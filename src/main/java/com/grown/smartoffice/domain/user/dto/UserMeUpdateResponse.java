package com.grown.smartoffice.domain.user.dto;

import com.grown.smartoffice.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserMeUpdateResponse {

    private String phone;
    private LocalDateTime updatedAt;

    public static UserMeUpdateResponse from(User user) {
        return UserMeUpdateResponse.builder()
                .phone(user.getPhone())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
