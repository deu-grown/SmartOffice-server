package com.grown.smartoffice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 400 Bad Request ──────────────────────────────────
    INVALID_INPUT(400, "잘못된 입력값입니다."),
    MISSING_REQUIRED_FIELD(400, "필수 항목이 누락되었습니다."),

    // ── 401 Unauthorized ─────────────────────────────────
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "만료된 액세스 토큰입니다."),
    UNAUTHENTICATED(401, "인증이 필요합니다."),

    // ── 403 Forbidden ────────────────────────────────────
    ACCOUNT_INACTIVE(403, "퇴사 처리된 계정입니다."),
    ACCESS_DENIED(403, "접근 권한이 없습니다."),
    REFRESH_TOKEN_EXPIRED(403, "Refresh Token이 만료되었습니다. 다시 로그인해주세요."),

    // ── 404 Not Found ────────────────────────────────────
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(404, "부서를 찾을 수 없습니다."),

    // ── 409 Conflict ─────────────────────────────────────
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),
    DUPLICATE_EMPLOYEE_NUMBER(409, "이미 사용 중인 사번입니다."),
    DUPLICATE_NFC_CARD(409, "이미 등록된 NFC 카드 UID입니다."),

    // ── 500 Internal Server Error ────────────────────────
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
