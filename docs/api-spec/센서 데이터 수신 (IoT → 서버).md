# 센서 데이터 수신 (IoT → 서버)

카테고리: 환경/센서
설명: IoT에서 서버로 센서 데이터를 수신합니다.
Method: POST
URL: /api/v1/sensors/logs
담당자: 우하
사용자: SYSTEM
우선순위: 2

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| zoneId | 구역 번호 | integer | 필수 | X | 1 |
| sensorType | 센서 타입 | string | 필수 | X | "TEMPERATURE" (하나만 있어도?) |
| value | 센서 값 | integer | 필수 | X | 24.5 |
| unit | 단위 | string | 필수 | X | "°C" |
| timestamp | 측정 시간 | string | 필수 | X | "2026-04-02T14:30:00" |

**Query parameter**

**Example**

```jsx
{
  "zoneId": 1,
  "sensorType": "TEMPERATURE",
  "value": 24.5,
  "unit": "°C",
  "timestamp": "2026-04-02T14:30:00"
}
```

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| status | 상태 | string |  | X | "success" |
| message | 메시지 | string |  | X | "데이터가 정상적으로 기록되었습니다." (필요한가?) |
| data | 데이터 | array |  | X | { "logId": 5001 } |
| logId | 로그 아이디 | integer |  | X | 5001 |

**Example**

```jsx
{
  "status": "success",
  "message": "데이터가 정상적으로 기록되었습니다.",
  "data": { "logId": 5001 }
}
```

### Status

| status | response content |
| --- | --- |
| 200 |  |
| 400 |  |