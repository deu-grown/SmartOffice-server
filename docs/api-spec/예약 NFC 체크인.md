# 예약 NFC 체크인

카테고리: 예약
설명: 예약 시작 시각 기준 ±10분 이내에 NFC 태그로 체크인한다. 체크인 성공 시 status를 CHECKED_IN으로 변경하고 checkedInAt을 기록한다. 예약 시작 10분 전부터 체크인 가능하며, 예약 종료 후에는 체크인 불가.
Method: POST
URL: /api/v1/reservations/{id}/check-in
담당자: 박성종
사용자: ALL
우선순위: 9

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| nfcTagId | 현장 NFC 태그의 고유 식별 번호 | String | 필수 | No | "NFC_ZONE_01_A9" |
| latitude | (선택) 체크인 시도 위치 위도 | Double | 선택 | Yes | 35.1796 |
| longitude | (선택) 체크인 시도 위치 경도 | Double | 선택 | Yes | 129.0756 |

**Header**

| key | 설명 | 예시 |
| --- | --- | --- |
| Authorization | 사용자/관리자 인증 토큰 (Bearer) | Bearer eyJhbGciOiJIUz... |
| Content-Type | 데이터 전송 형식 | application/json |

**Query parameter**

X

**Example (Request Body)**

```json
{
  "nfcTagId": "NFC_ZONE_01_A9",
  "latitude": 35.1796,
  "longitude": 129.0756
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| status | 응답 성공 여부 | String | 필수 | No | "success" |
| message | 결과 메시지 | String | 필수 | No | "체크인이 성공적으로 완료되었습니다." |
| data.checkInTime | 실제 체크인 처리 시각 | String | 필수 | No | "2026-04-02T14:02:15" |
| data.status | 변경된 예약 상태 | String | 필수 | No | "IN_USE" |

**Example**

```json
{
  "status": "success",
  "message": "체크인이 성공적으로 완료되었습니다.",
  "data": {
    "checkInTime": "2026-04-02T14:02:15",
    "status": "IN_USE"
  }
}
```

### Status

| status | response content | 설명 |
| --- | --- | --- |
| 200 | OK | 요청 성공 (조회, 수정, 삭제) |
| 201 | Created | 예약 생성 성공 |
| 400 | Bad Request | 예약 시간 중복 또는 잘못된 형식 |
| 401 | Unauthorized | 인증 실패 (토큰 만료 등) |
| 403 | Forbidden | 권한 없음 (본인 예약이 아닌 경우 등) |
| 404 | Not Found | 존재하지 않는 예약 번호 |