package com.grown.smartoffice.global.exception;

import com.grown.smartoffice.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 예외 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.warn("[CustomException] {} - {}", e.getErrorCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getMessage()));
    }

    /** @Valid 검증 실패 (400) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        log.warn("[Validation] {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /** JSON 파싱 오류 (400) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[NotReadable] {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error("요청 본문을 읽을 수 없습니다."));
    }

    /** 그 외 모든 예외 (500) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[UnhandledException]", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
