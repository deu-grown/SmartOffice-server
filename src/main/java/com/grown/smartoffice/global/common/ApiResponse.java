package com.grown.smartoffice.global.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("success", message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}
