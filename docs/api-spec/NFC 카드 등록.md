# NFC 카드 등록

카테고리: NFC 카드
설명: 직원에게 NFC 카드를 발급한다. 1인 1카드 원칙으로 ACTIVE 상태 카드가 이미 존재하면 등록 불가.
Method: POST
URL: /api/v1/nfc-cards
담당자: 우하
사용자: ADMIN
우선순위: 4

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| userId | 소유 직원 ID | Long |  | X | 1 |
| uid | NFC 태그 UID | String |  | X | "04:A3:2F:1B" |
| cardType | 카드 유형 | String |  | X | "EMPLOYEE" |
| expiredAt | 만료일시 | String |  | O | "2027-03-02T00:00:00" |

**Header**

| key | 설명 | 예시 |
| --- | --- | --- |
| Authorization | Bearer Access Token | "Bearer eyJhbGci..." |

**Query parameter**

X

**Example (Request Body)**

```json
{
  "userId": 1,
  "uid": "04:A3:2F:1B",
  "cardType": "EMPLOYEE",
  "expiredAt": "2027-03-02T00:00:00"
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "NFC 카드가 발급되었습니다." |
| data.id | 생성된 카드 ID | Long |  | X | 1 |
| data.uid | NFC 태그 UID | String |  | X | "04:A3:2F:1B" |
| data.cardType | 카드 유형 | String |  | X | "EMPLOYEE" |
| data.status | 카드 상태 | String |  | X | "ACTIVE" |
| data.userId | 소유 직원 ID | Long |  | X | 1 |
| data.userName | 소유 직원 이름 | String |  | X | "박성종" |
| data.issuedAt | 발급일시 | String |  | X | "2026-04-01T09:00:00" |
| data.expiredAt | 만료일시 | String |  | O | "2027-03-02T00:00:00" |

**Example**

```json
{
  "code": "success",
  "message": "NFC 카드가 발급되었습니다.",
  "data": {
    "id": 1,
    "uid": "04:A3:2F:1B",
    "cardType": "EMPLOYEE",
    "status": "ACTIVE",
    "userId": 1,
    "userName": "박성종",
    "issuedAt": "2026-04-01T09:00:00",
    "expiredAt": "2027-03-02T00:00:00"
  }
}
```

### Status

| status | response content |
| --- | --- |
| 201 | NFC 카드 발급 성공 |
| 400 | 요청 값 누락 또는 형식 오류 |
| 401 | Access Token 누락 또는 유효하지 않음 |
| 403 | 관리자 권한 없음 |
| 404 | 존재하지 않는 직원 ID |
| 409 | 이미 등록된 UID |
| 409 | 해당 직원에게 ACTIVE 상태 카드가 이미 존재함 |