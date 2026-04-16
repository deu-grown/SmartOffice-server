# 주차 상태 업데이트 (IoT → 서버)

카테고리: 주차
설명: 초음파 센서 감지 결과를 서버로 전송한다. parking_spots.is_occupied를 업데이트하고 sensor_logs에 raw 데이터를 기록한다.
Method: POST
URL: /api/v1/parking/spots/{id}/status
담당자: 우하
사용자: SYSTEM
우선순위: 6

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| deviceId | 초음파 센서 장치 ID | Long |  | X | 15 |
| isOccupied | 점유 여부 | Boolean |  | X | true |
| distanceCm | 측정 거리 (cm) | Double |  | O | 8.3 |
| measuredAt | 측정 일시 (Pi 로컬 시각) | String |  | X | "2026-04-01T14:35:00" |

**Header**

X

**Query parameter**

X

**Example (Request Body)**

```json
{
  "deviceId": 15,
  "isOccupied": true,
  "distanceCm": 8.3,
  "measuredAt": "2026-04-01T14:35:00"
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| code | 상태값 | String |  | X | "success" |
| message | 메시지 | String |  | X | "주차 상태가 업데이트되었습니다." |
| data.spotId | 주차면 ID | Long |  | X | 1 |
| data.spotNumber | 주차면 번호 | String |  | X | "A-01" |
| data.isOccupied | 변경된 점유 여부 | Boolean |  | X | true |
| data.measuredAt | 측정 일시 | String |  | X | "2026-04-01T14:35:00" |

**Example**

```json
{
  "code": "success",
  "message": "주차 상태가 업데이트되었습니다.",
  "data": {
    "spotId": 1,
    "spotNumber": "A-01",
    "isOccupied": true,
    "measuredAt": "2026-04-01T14:35:00"
  }
}
```

### Status

| status | response content |
| --- | --- |
| 200 | 주차 상태 업데이트 성공 |
| 400 | 요청 값 누락 또는 형식 오류 |
| 404 | 존재하지 않는 주차면 또는 장치 ID |
| 409 | 해당 장치가 이 주차면에 연결되어 있지 않음 |