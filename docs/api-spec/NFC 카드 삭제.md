# NFC 카드 삭제

카테고리: NFC 카드
설명: NFC 카드를 삭제한다. 출입 로그에 연관된 카드는 삭제 불가. 분실 카드는 삭제 대신 status를 LOST로 변경 권장.
Method: DELETE
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
| message | 메시지 | String |  | X | "NFC 카드가 삭제되었습니다." |

**Example**

```json
{
  "code": "success",
  "message": "NFC 카드가 삭제되었습니다."
}
```

### Status

| status | response content |
| --- | --- |
| 200 | NFC 카드 삭제 성공 |
| 401 | Access Token 누락 또는 유효하지 않음 |
| 403 | 관리자 권한 없음 |
| 404 | 존재하지 않는 카드 |
| 409 | 출입 로그에 연관된 카드로 삭제 불가 |