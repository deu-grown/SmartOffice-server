# NFC 카드 수정 (분실 처리 등)

카테고리: NFC 카드
설명: NFC 카드 상태 및 만료일을 수정한다. 분실 신고, 카드 비활성화, 만료일 연장 등에 사용한다.
Method: PUT
URL: /api/v1/nfc-cards/{id}
담당자: 우하
사용자: ADMIN
우선순위: 4

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| status | 카드 상태 | String |  | O | "LOST" |
| expiredAt | 만료일시 | String |  | O | "2028-03-02T00:00:00" |

**Header**

| key | 설명 | 예시 |
| --- | --- | --- |
| Authorization | Bearer Access Token | "Bearer eyJhbGci..." |

**Query parameter**

X

**Example (Request Body)**

```json
{
  "status": "LOST",
  "expiredAt": "2028-03-02T00:00:00"
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "카드 정보가 수정되었습니다." |
| data.id | 카드 ID | Long |  | X | 1 |
| data.uid | NFC 태그 UID | String |  | X | "04:A3:2F:1B" |
| data.status | 변경된 카드 상태 | String |  | X | "LOST" |
| data.expiredAt | 만료일시 | String |  | O | "2028-03-02T00:00:00" |
| data.updatedAt | 수정일시 | String |  | X | "2026-04-01T10:00:00" |

**Example**

```json
{
  "code": "success",
  "message": "카드 정보가 수정되었습니다.",
  "data": {
    "id": 1,
    "uid": "04:A3:2F:1B",
    "status": "LOST",
    "expiredAt": "2028-03-02T00:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  }
}
```

### Status

| status | response content |
| --- | --- |
| 200 | NFC 카드 수정 성공 |
| 400 | 요청 값 형식 오류 |
| 401 | Access Token 누락 또는 유효하지 않음 |
| 403 | 관리자 권한 없음 |
| 404 | 존재하지 않는 카드 |