# RFID 태그 감지 이벤트 처리

카테고리: 자산관리
설명: RFID 리더기에서 태그 감지 시 서버로 전송한다. 감지된 구역이 자산의 허용 구역과 불일치하면 asset_alerts를 생성하고 관리자에게 알림을 발생시킨다.
Method: POST
URL: /api/v1/assets/rfid/tag
담당자: 박성종
사용자: SYSTEM
우선순위: 8

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| deviceId | 감지한 RFID 리더기 장치 ID | Long |  | X | 10 |
| rfidUid | 감지된 RFID 태그 UID | String |  | X | "RF:A1:B2:C3" |
| detectedAt | 감지 일시 (Pi 로컬 시각) | String |  | X | "2026-04-01T14:32:00" |

**Header**

X

**Query parameter**

X

**Example (Request Body)**

```json
{
  "deviceId": 10,
  "rfidUid": "RF:A1:B2:C3",
  "detectedAt": "2026-04-01T14:32:00"
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "정상 구역 내 감지입니다." |
| data.assetId | 자산 ID | Long |  | O | 1 |
| data.assetName | 자산명 | String |  | O | "맥북 프로 16인치" |
| data.detectedZoneId | 감지된 구역 ID | Long |  | X | 3 |
| data.detectedZoneName | 감지된 구역명 | String |  | X | "1층 서버실" |
| data.allowedZoneId | 허용 구역 ID | Long |  | O | 2 |
| data.allowedZoneName | 허용 구역명 | String |  | O | "2층 개발팀" |
| data.isViolation | 이탈 여부 | Boolean |  | X | true |
| data.alertId | 생성된 알림 ID | Long |  | O | 5 |

**Example**

```json
{
  "code": "success",
  "message": "정상 구역 내 감지입니다.",
  "data": {
    "assetId": 1,
    "assetName": "맥북 프로 16인치",
    "detectedZoneId": 2,
    "detectedZoneName": "2층 개발팀",
    "allowedZoneId": 2,
    "allowedZoneName": "2층 개발팀",
    "isViolation": false,
    "alertId": null
  }
}
```

**Example (이탈 감지)**

```json
{
  "code": "success",
  "message": "허용 구역 이탈이 감지되었습니다.",
  "data": {
    "assetId": 1,
    "assetName": "맥북 프로 16인치",
    "detectedZoneId": 3,
    "detectedZoneName": "1층 서버실",
    "allowedZoneId": 2,
    "allowedZoneName": "2층 개발팀",
    "isViolation": true,
    "alertId": 5
  }
}
```

### Status

| status | response content |
| --- | --- |
| 200 | RFID 태그 감지 처리 성공 (정상/이탈 모두 200 반환) |
| 400 | 요청 값 누락 또는 형식 오류 |
| 404 | 존재하지 않는 장치 ID |