# NFC 태그 이벤트 처리

카테고리: 출입 로그
설명: NFC 리더기에서 태그 이벤트 발생 시 서버로 전송한다. 등록된 UID면 APPROVED, 미등록이거나 비활성 카드면 DENIED로 처리한다. access_logs에 raw 기록 후 결과를 응답한다.
Method: POST
URL: /api/v1/access-logs/tag
담당자: 우하
사용자: SYSTEM
우선순위: 5

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| deviceId | 태그된 NFC 리더기 ID | Long |  | X | 1 |
| uid | 태그된 NFC UID | String |  | X | "04:A3:2F:1B" |
| direction | 입실/퇴실 방향 | String |  | X | "IN" |
| taggedAt | 태그 발생 일시 (Pi 로컬 시각) | String |  | X | "2026-04-01T09:02:11" |

**Header**

X

**Query parameter**

X

**Example (Request Body)**

```json
{
  "deviceId": 1,
  "uid": "04:A3:2F:1B",
  "direction": "IN",
  "taggedAt": "2026-04-01T09:02:11"
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "출입이 승인되었습니다." |
| data.accessLogId | 생성된 출입 로그 ID | Long |  | X | 1024 |
| data.authResult | 인증 결과 | String |  | X | "APPROVED" |
| data.denyReason | 거부 사유 | String |  | O | null |
| data.userId | 인증된 직원 ID | Long |  | O | 1 |
| data.userName | 인증된 직원 이름 | String |  | O | "박성종" |
| data.zoneId | 출입 구역 ID | Long |  | X | 1 |
| data.zoneName | 출입 구역명 | String |  | X | "1층" |
| data.direction | 입실/퇴실 방향 | String |  | X | "IN" |
| data.taggedAt | 태그 일시 | String |  | X | "2026-04-01T09:02:11" |

**Example**

```json
{
  "code": "success",
  "message": "출입이 승인되었습니다.",
  "data": {
    "accessLogId": 1024,
    "authResult": "APPROVED",
    "denyReason": null,
    "userId": 1,
    "userName": "박성종",
    "zoneId": 1,
    "zoneName": "1층",
    "direction": "IN",
    "taggedAt": "2026-04-01T09:02:11"
  }
}
```

**DENIED 응답 Example**

```json
{
  "code": "success",
  "message": "출입이 거부되었습니다.",
  "data": {
    "accessLogId": 1025,
    "authResult": "DENIED",
    "denyReason": "등록되지 않은 카드입니다.",
    "userId": null,
    "userName": null,
    "zoneId": 1,
    "zoneName": "1층",
    "direction": "IN",
    "taggedAt": "2026-04-01T09:03:45"
  }
}
```

### Status

| status | response content |
| --- | --- |
| 200 | 태그 이벤트 처리 성공 (APPROVED/DENIED 모두 200 반환) |
| 400 | 요청 값 누락 또는 형식 오류 |
| 404 | 존재하지 않는 장치 ID |