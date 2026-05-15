package com.grown.smartoffice.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final String code;

    /**
     * 에러 식별자 (ErrorCode enum name). 정상 응답에서는 null 이며 직렬화에서 제외된다.
     * 클라이언트가 message 문자열 매칭 대신 안정적인 식별자로 에러 유형을 분기할 수 있다.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorCode;

    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", null, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>("success", null, message, null);
    }

    public static ApiResponse<Void> error(String errorCode, String message) {
        return new ApiResponse<>("error", errorCode, message, null);
    }
}
