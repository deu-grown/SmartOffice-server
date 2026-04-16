# 전체 NFC 카드 목록 조회

카테고리: NFC 카드
설명: 전체 NFC 카드 목록을 조회한다. 카드 유형, 상태, 직원으로 필터링 가능.
Method: GET
URL: /api/v1/nfc-cards
param: userId, cardType, status
담당자: 우하
사용자: ADMIN
우선순위: 4

### Request

X

**Header**

| key | 설명 | 예시 |
| --- | --- | --- |
| Authorization | Bearer Access Token | "Bearer eyJhbGci..." |

**Query parameter**

| key | 설명 | 타입 | Nullable | 예시 |
| --- | --- | --- | --- | --- |
| userId | 직원 ID 필터 | Long | O | 1 |
| cardType | 카드 유형 필터 | String | O | "EMPLOYEE" |
| status | 카드 상태 필터 | String | O | "ACTIVE" |

**Example (Request Body)**

X

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "정상 조회되었습니다." |
| data[].id | 카드 ID | Long |  | X | 1 |
| data[].uid | NFC 태그 UID | String |  | X | "04:A3:2F:1B" |
| data[].cardType | 카드 유형 | String |  | X | "EMPLOYEE" |
| data[].status | 카드 상태 | String |  | X | "ACTIVE" |
| data[].userId | 소유 직원 ID | Long |  | X | 1 |
| data[].userName | 소유 직원 이름 | String |  | X | "박성종" |
| data[].employeeNumber | 사번 | String |  | X | "EMP001" |
| data[].issuedAt | 발급일시 | String |  | X | "2026-03-02T09:00:00" |
| data[].expiredAt | 만료일시 | String |  | O | "2027-03-02T00:00:00" |

**Example**

```json
{
  "code": "success",
  "message": "정상 조회되었습니다.",
  "data": [
    {
      "id": 1,
      "uid": "04:A3:2F:1B",
      "cardType": "EMPLOYEE",
      "status": "ACTIVE",
      "userId": 1,
      "userName": "박성종",
      "employeeNumber": "EMP001",
      "issuedAt": "2026-03-02T09:00:00",
      "expiredAt": "2027-03-02T00:00:00"
    },
    {
      "id": 2,
      "uid": "04:B7:1C:3D",
      "cardType": "EMPLOYEE",
      "status": "LOST",
      "userId": 2,
      "userName": "김동우",
      "employeeNumber": "EMP002",
      "issuedAt": "2026-03-02T09:00:00",
      "expiredAt": "2027-03-02T00:00:00"
    }
  ]
}
```

### Status

| status | response content |
| --- | --- |
| 200 | NFC 카드 목록 조회 성공 |
| 401 | Access Token 누락 또는 유효하지 않음 |
| 403 | 관리자 권한 없음 |