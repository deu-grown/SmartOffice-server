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
    NFC_CARD_EXPIRED(403, "만료된 NFC 카드입니다."),

    // ── 404 Not Found ────────────────────────────────────
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(404, "부서를 찾을 수 없습니다."),
    ZONE_NOT_FOUND(404, "구역을 찾을 수 없습니다."),
    PARENT_ZONE_NOT_FOUND(404, "상위 구역을 찾을 수 없습니다."),
    DEVICE_NOT_FOUND(404, "장치를 찾을 수 없습니다."),
    NFC_CARD_NOT_FOUND(404, "등록되지 않은 NFC 카드입니다."),

    // ── 400 Bad Request (Zone) ────────────────────────────
    INVALID_ZONE_HIERARCHY(400, "자기 자신 또는 하위 구역을 상위로 지정할 수 없습니다."),

    // ── 409 Conflict ─────────────────────────────────────
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),
    DUPLICATE_EMPLOYEE_NUMBER(409, "이미 사용 중인 사번입니다."),
    DUPLICATE_NFC_CARD(409, "이미 등록된 NFC 카드 UID입니다."),
    DUPLICATE_DEPARTMENT_NAME(409, "이미 존재하는 부서명입니다."),
    DEPARTMENT_HAS_USERS(409, "소속 직원이 존재하여 삭제할 수 없습니다."),
    DUPLICATE_ZONE_NAME(409, "동일 상위 구역 내 중복 구역명입니다."),
    ZONE_HAS_CHILDREN(409, "하위 구역이 존재하여 삭제할 수 없습니다."),
    ZONE_HAS_DEVICES(409, "설치된 장치가 존재하여 삭제할 수 없습니다."),
    USER_ALREADY_INACTIVE(409, "이미 퇴사 처리된 직원입니다."),

    // ── 422 Unprocessable Entity ─────────────────────────
    WRONG_PASSWORD(422, "현재 비밀번호가 일치하지 않습니다."),

    // ── 500 Internal Server Error ────────────────────────
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
