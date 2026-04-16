# NFC 카드 상세 조회

카테고리: NFC 카드
설명: 특정 NFC 카드의 상세 정보를 조회한다.
Method: GET
URL: /api/v1/nfc-cards/{id}
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

X

**Example (Request Body)**

X

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "정상 조회되었습니다." |
| data.id | 카드 ID | Long |  | X | 1 |
| data.uid | NFC 태그 UID | String |  | X | "04:A3:2F:1B" |
| data.cardType | 카드 유형 | String |  | X | "EMPLOYEE" |
| data.status | 카드 상태 | String |  | X | "ACTIVE" |
| data.userId | 소유 직원 ID | Long |  | X | 1 |
| data.userName | 소유 직원 이름 | String |  | X | "박성종" |
| data.employeeNumber | 사번 | String |  | X | "EMP001" |
| data.department | 부서명 | String |  | O | "개발팀" |
| data.issuedAt | 발급일시 | String |  | X | "2026-03-02T09:00:00" |
| data.expiredAt | 만료일시 | String |  | O | "2027-03-02T00:00:00" |
| data.createdAt | 등록일시 | String |  | X | "2026-03-02T09:00:00" |
| data.updatedAt | 수정일시 | String |  | X | "2026-04-01T10:00:00" |

**Example**

```json
{
  "code": "success",
  "message": "정상 조회되었습니다.",
  "data": {
    "id": 1,
    "uid": "04:A3:2F:1B",
    "cardType": "EMPLOYEE",
    "status": "ACTIVE",
    "userId": 1,
    "userName": "박성종",
    "employeeNumber": "EMP001",
    "department": "개발팀",
    "issuedAt": "2026-03-02T09:00:00",
    "expiredAt": "2027-03-02T00:00:00",
    "createdAt": "2026-03-02T09:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  }
}
```

### Status

| status | response content |
| --- | --- |
| 200 | NFC 카드 상세 조회 성공 |
| 401 | Access Token 누락 또는 유효하지 않음 |
| 403 | 관리자 권한 없음 |
| 404 | 존재하지 않는 카드 |