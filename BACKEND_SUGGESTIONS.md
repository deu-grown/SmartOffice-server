# BACKEND_SUGGESTIONS.md — 프론트엔드 통합 작업에서 발견·도출된 백엔드 제안

> 본 문서는 `SmartOffice-web` 통합 작업(2026-05) 중 발견된 백엔드 측 보완 항목을 우선순위와 근거와 함께 정리한 것이다. 이번 통합 작업에서는 백엔드 코드를 직접 수정하지 않았다. 각 항목은 백엔드 담당자가 채택 여부를 판단해 별도 PR 로 진행한다.

---

## 1. (중) `guest` 도메인 신설 — 방문객 관리 API

**근거**
- 프론트 `SmartOffice-web/src/components/dashboard/GuestTable.tsx` 가 외주 단계에서 mock 30+ 건으로 방문객 UI 가 구현되어 있다.
- 백엔드에는 `guest` 별도 도메인이 없고, 가장 유사한 `reservations` 는 직원 회의실 예약용이라 **외부인 식별 필드**(회사·연락처·호스트·방문 목적·체크인/체크아웃)가 부족하다.
- 본 통합 작업에서는 사이드바 메뉴를 **임시 숨김** 처리하고 `/guest` 라우트는 대시보드로 리다이렉트한다.

**제안 범위**
- 테이블: `guests` (id, name, company, host_user_id, purpose, status enum {SCHEDULED|VISITING|COMPLETED|CANCELLED}, scheduled_entry_at, actual_entry_at, actual_exit_at, contact_phone, ...)
- API 7~8 개:
  - `POST /api/v1/guests` (등록·예약, ADMIN 또는 host USER)
  - `GET /api/v1/guests` (목록·필터, ADMIN)
  - `GET /api/v1/guests/{id}` (상세)
  - `PUT /api/v1/guests/{id}` (수정)
  - `DELETE /api/v1/guests/{id}` (취소)
  - `POST /api/v1/guests/{id}/check-in` (방문 시작)
  - `POST /api/v1/guests/{id}/check-out` (방문 종료)
- (선택) NFC 임시 카드 발급과 연동하면 `access_logs` 와 자연스럽게 연결됨.

**우선순위 근거**: 프론트 UI 가 이미 외주 단계에서 들어와 있어 mock 의존도가 가장 높다. 도입 시 사이드바 메뉴 즉시 복구 가능.

---

## 2. (저) Refresh Token httpOnly 쿠키 전환 — 후속 제안

**근거**
- 현재 Access·Refresh Token 을 프론트가 `localStorage` 에 보관한다. XSS 시 토큰 탈취 위험.
- httpOnly + Secure + SameSite=Lax(Strict) 쿠키로 전환하면 자바스크립트 접근 차단으로 XSS 완화.

**동반 변경**
- 백엔드 `SecurityConfig`: CORS allow-credentials, exposed/allowed headers, SameSite 설정
- `AuthController.login` 응답에서 `Set-Cookie` 발급 (`refreshToken`)
- `AuthController.refresh` 는 쿠키에서 refresh token 을 읽음 (현재는 body 의 `refreshToken`)
- 운영 환경 도메인이 같지 않다면 `Domain` 속성과 도메인 통일 필요
- 프론트 axios `withCredentials: true` 활성화 (`src/lib/api/client.ts`), `tokenStorage.refreshToken` 관련 함수 제거

**본 통합 작업에서는 채택하지 않음** — SecurityConfig·운영 도메인·CORS 정책 변경이 동반되므로 별도 보안 강화 작업으로 분리.

---

## 3. (저) "시스템 설정" 페이지용 API 정리

**근거**
- 프론트 사이드바에 "시스템 설정" 항목이 존재하고, TopBar 의 드롭다운에서 마이페이지/프로필/시스템 설정 모달이 노출되어 있다.
- 현재 본인 정보 조회/수정은 `GET /api/v1/auth/me` 와 `POST /api/v1/users/me` (UserMeUpdateRequest) 로 갈음 가능하다.
- 알림 on/off · 언어 · 테마 · 푸시 토큰 등 **사용자 환경설정(per-user preferences)** 을 저장할 엔드포인트가 필요한지 검토 권장.

**제안 범위 (필요 시)**
- 테이블: `user_preferences` (user_id, notifications_enabled, language, theme, push_token, updated_at)
- API: `GET /api/v1/users/me/preferences`, `PUT /api/v1/users/me/preferences`
- 또는 `User` 엔티티에 컬럼 추가 (단순할 경우)

본 통합 작업에서는 "시스템 설정" 라우트를 *준비 중* placeholder 로 두고 진행했다.

---

## 4. (저) Dashboard 응답 타입 OpenAPI 보강 + openapi-typescript 검토

**근거**
- `springdoc-openapi` 가 동작하지만 일부 응답 DTO 의 schema 가 누락 필드/예시값 없이 노출될 수 있음.
- 프론트가 향후 `openapi-typescript` 등으로 OpenAPI JSON 을 입력받아 TS 타입을 자동 생성한다면, 백엔드 schema 정합성이 직접적인 빌드 품질에 영향.

**부가 제안**
- 프론트 측에서 `openapi-typescript` 도입을 검토할 수 있도록 백엔드 OpenAPI schema 의 다음을 점검 권장:
  - 필드 누락 (특히 nested object · enum)
  - nullable / required 표기 일관성
  - 예시값(`@Schema(example = ...)`) — 자동 타입 생성에는 영향 없지만 Swagger UI 가독성에 기여
- 본 통합 작업의 프론트 `src/features/{domain}/types.ts` 는 수동 1:1 매핑 중. 도메인 수가 늘면 자동 생성 검토 가치 ↑.

---

## 5. (중) 에러 응답 페이로드에 `errorCode` 식별자 필드 추가

**발생 맥락**
- `SmartOffice-web` 플랜 2 인증 도메인 마이그레이션 중 프론트 에러 처리 구현 시 발견.
- 현재 `ApiResponse.code` 는 `"success" | "error"` 두 값만 노출되고, 47 개 `ErrorCode` enum 식별자는 `message` 한국어 문자열에만 포함된다.
- 프론트는 식별자가 없으니 message 문자열 매칭이라는 비정상 경로로 분기해야 하는 상황.

**제안 범위**
- `ApiResponse` 에러 응답에 `errorCode` 선택 필드 추가. 예:
  ```json
  { "code": "error", "errorCode": "INVALID_CREDENTIALS", "message": "비밀번호가 일치하지 않습니다.", "data": null }
  ```
- 값은 `ErrorCode.name()` (enum 식별자) 또는 안정 식별자(예: `"AUTH_001"`) 중 택1. 한 번 정하면 깨지 않는 계약으로 운영.
- `GlobalExceptionHandler` 와 `CustomException` 경로에서 `ErrorCode` → 응답 필드로 전파.

**근거**
- 프론트 에러 유형별 분기 처리 가능 (예: `INVALID_CREDENTIALS` → 비밀번호 필드 강조, `ACCOUNT_INACTIVE` → 별도 안내 모달).
- 향후 i18n 도입 시 메시지 자체를 클라이언트가 번역 (백엔드 message 의존 분리).
- `openapi-typescript` 도입 시 enum 자동 생성으로 타입 안전 분기 구현 (위 4번 항목과 시너지).

**우선순위 근거**: 현재 동작에 결함은 없으나 message 문자열 매칭이라는 비정상 분기를 즉시 안정화하고, 후속 작업(i18n · 자동 타입 생성)의 전제 조건이 된다.

**출처 세션**: `SmartOffice-web` 플랜 2 인증 도메인 마이그레이션 (2026-05).

---

## 6. (하 / 검토) `reservation` 도메인 service-layer 본인/ADMIN 분기 일관성 점검

**발생 맥락**
- `SmartOffice-web` 플랜 3 사전 분석 중 `reservation` 8 개 엔드포인트 권한을 정리하다가 발견.
- `GET / PUT / DELETE /api/v1/reservations/{id}` 는 컨트롤러 단에서 `@PreAuthorize` 없이 기본 인증만 요구하고, 본인/ADMIN 분기는 `ReservationService` 가 `userDetails.getUsername()` 기반으로 수행.
- 반면 `GET /api/v1/reservations` (전체 목록) 는 컨트롤러에서 `@PreAuthorize("hasRole('ADMIN')")` 로 명시.
- 즉 같은 도메인 안에서 권한 검증 위치(컨트롤러 vs service)가 엔드포인트마다 갈린다.

**제안 범위**
- 컨트롤러 단 `@PreAuthorize` 와 service 단 본인/ADMIN 검증의 **역할 분담을 문서화** (백엔드 `CLAUDE.md` "보안" 절 보강 등).
- 또는 본인/ADMIN 공용 엔드포인트에 표준 어노테이션(예: `@SelfOrAdmin`)·유틸을 도입해 패턴 통일.
- 다른 도메인(예: `user`, `attendance`, `salary`)에 동일 패턴이 있다면 함께 점검.

**근거**
- 새 엔드포인트 추가 시 권한 검증 위치를 어느 레이어에 둘지 즉시 판단 가능.
- OpenAPI / Swagger 문서에서 권한 요구사항이 정확히 노출됨 (현재 service-layer 검증은 OpenAPI 메타데이터에 드러나지 않아 클라이언트 입장에서 "그냥 인증 필요" 로만 보임).
- 향후 권한 정책 변경(예: 부서장 권한 추가, 호스트 권한 추가) 시 변경 지점 일관성 확보.

**우선순위 근거**: 현재 동작에 문제 없음. 일관성·유지보수성·문서 정확도 차원의 개선이라 후순위.

**출처 세션**: `SmartOffice-web` 플랜 3 사전 분석 — `reservation` 도메인 권한 분석 (2026-05).

---

## 7. (상) `GET /api/v1/dashboard/summary` HTTP 500 — 데이터 부재 시 NPE 추정

**발생 맥락**
- `SmartOffice-web` 플랜 3-1 묶음 2(G2 통합 관제) 백엔드 curl 검증 중 발견 (2026-05-14).
- 동일 세션에서 admin@grown.com Bearer 토큰으로 `GET /api/v1/dashboard/summary` 호출 → HTTP 500.
- 응답 본문: `{"code":"error","data":null,"message":"서버 내부 오류가 발생했습니다."}`
- 같은 토큰으로 호출한 다른 dashboard 3종 (`/sensors/current`, `/attendance/today`, `/access/recent?limit=10`) 은 모두 정상 HTTP 200 → 권한·JWT 문제는 아님.

**재현**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@grown.com","password":"EMP001"}' | jq -r '.data.accessToken')
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/dashboard/summary
```

**추정 원인**
- `DashboardService.getSummary` 내부 NPE 또는 데이터 부재 시 예외.
  - 예: `userRepository.count()` 결과는 정수라 NPE 가능성 낮음.
  - 예: `reservationRepository.findTodayCount()` 등에서 `today` 기준 결과가 비었을 때 `.size()` 호출 전 null 반환?
  - 예: `deviceRepository.countByStatus(...)` 같은 쿼리가 enum 매칭 실패?
- 백엔드 콘솔 로그(`./gradlew bootRun` 출력) 의 스택트레이스로 정확 원인 식별 필요.

**제안**
- `DashboardService.getSummary` 내부에 null safety 추가 (모든 카운트 메서드의 null 반환 처리).
- 빈 데이터 케이스를 예외 throw 가 아닌 명시 응답 (모든 카운트 = 0) 으로 처리.
- DTO `DashboardSummaryResponse` 의 4 필드 (`totalUsers / todayReservations / activeDevices / pendingApprovals`) 가 항상 정수 0 이상 보장되도록 서비스 레이어에서 정규화.
- 회귀 방지를 위해 `DashboardServiceTest` 에 "데이터 없음" 케이스 단위 테스트 추가.

**프론트 영향**
- `SmartOffice-web` `IntegratedDashboard` 의 KPI 카드 4종 (현재 출근 인원·오늘 예약·활성 장치·전체 사용자) 이 영구 실패 상태.
- 위젯 단위 `ErrorBoundary` / `useDashboardSummary` 의 `isError` 처리 덕분에 페이지 깨짐은 없으나, **핵심 KPI 미표시** = 대시보드의 first visible content 손실.
- 백엔드 수정 후 즉시 프론트 재검증 필요 (curl + 브라우저 `/dashboard`).

**우선순위**: 상. KPI 카드는 대시보드의 first visible content. 다른 dashboard 3종 응답은 정상이므로 본 1건 수정만으로 G2 완성도 회복.

**출처 세션**: `SmartOffice-web` 플랜 3-1 묶음 2 백엔드 curl 검증 (2026-05-14).

**처리 완료**: 백엔드 수정 sprint 묶음 1 (`d96e77d` fix + `5edb416` test, 2026-05-15) — `DeviceRepository.countByDeviceStatus(String)` ↔ `Device.deviceStatus` enum 타입 불일치 (Hibernate 7.2.7 InvalidDataAccessApiUsageException) 가 진짜 원인. 시그니처를 `DeviceStatus` enum 으로 변경 + `DashboardService.getSummary` 호출도 `DeviceStatus.ACTIVE` enum 으로 변경. curl `GET /dashboard/summary` 200 + 4 필드 모두 정수 (`{activeDevices:18, todayReservations:5, totalUsers:10, pendingApprovals:0}`). web `/dashboard` KPI 4종 (현재 출근 0/10, 오늘 예약 5, 활성 장치 18, 전체 사용자 10) 복원 시각 검증 통과. `DashboardServiceTest.getSummary_emptyData` 데이터 없음 케이스 단위 테스트 추가.

---

## 8. (중) `access_logs.auth_result` 시드 데이터 정합성 — V5 의 `ALLOW` 값 잔존

**발생 맥락**
- `SmartOffice-web` 플랜 3-1 묶음 4(G4 출입 기록) 백엔드 curl 검증 (2026-05-14).
- `GET /api/v1/access-logs?authResult=ALLOW` 가 `totalElements=8` 으로 응답 — V5 시드 데이터의 잔존.
- 같은 의미(인증 성공)에 대해 `APPROVED`(53건) 와 `ALLOW`(8건) 두 값이 공존.

**재현**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@grown.com","password":"EMP001"}' | jq -r '.data.accessToken')

# 코드 표준(APPROVED) 응답
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/access-logs?authResult=APPROVED&size=1" | jq '.data.totalElements'
# → 53

# V5 시드 잔존(ALLOW) 응답
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/access-logs?authResult=ALLOW&size=1" | jq '.data.totalElements'
# → 8
```

**근거**
- `AccessLogService` 의 NFC 태그 처리 코드는 `"APPROVED"` / `"DENIED"` 만 사용 (BLOCKED 는 별도 분기에서 추가).
- `V8__seed_demo_dataset.sql` 주석 (L298-L300) 도 다음과 같이 명시:
  ```
  -- ⑫ 출입 로그 — 다양한 사용자 × 30일치 × ALLOW/DENIED/BLOCKED 혼합 (총 ~80건)
  --   * 코드는 'APPROVED' / 'DENIED' / 'BLOCKED' 를 사용
  --     V5는 'ALLOW' 사용 (기존 데이터 보존). V8는 코드 기준.
  ```
- 즉 V5/V8 시드 정책 차이가 의도적으로 보존된 상태이나, 코드 표준 단일 enum 으로 통일하는 편이 클라이언트·DB 모두 정합성 측면에서 안전.

**제안**
- 신규 Flyway 마이그레이션 (예: `V9__migrate_allow_to_approved.sql`) 에서
  `UPDATE access_logs SET auth_result = 'APPROVED' WHERE auth_result = 'ALLOW';` 1회 실행.
- 또는 V5 시드 SQL 자체를 'APPROVED' 로 정정 (단 운영 환경 이미 적용된 DB 가 있다면 별도 마이그레이션 필요).
- 향후 enum 컬럼화 검토 — `String` → DB enum 또는 Java `AuthResult` enum 으로 강타입화하면 본 류 정합성 이슈를 컴파일 단계에서 방지.

**프론트 영향**
- `SmartOffice-web/src/features/accesslog/types.ts` 의 `AccessLogAuthResult` literal union 을 `"APPROVED" | "DENIED" | "BLOCKED" | "ALLOW"` 로 정의해 호환 처리 중.
- `AccessRecordTable` UI 의 라벨 매핑(`AUTH_LABEL`) 에서 `APPROVED` 와 `ALLOW` 를 동일 "정상" 라벨로 매핑.
- 백엔드 마이그레이션 완료 시 클라이언트의 `ALLOW` 분기를 단계적으로 제거 가능.

**우선순위**: 중. 운영 동작 영향 없음 (단순 조회 분기). 데이터 정합성 + 클라이언트 enum 단순화 차원의 개선.

**출처 세션**: `SmartOffice-web` 플랜 3-1 묶음 4 백엔드 curl 검증 (2026-05-14).

**처리 완료**: 백엔드 수정 sprint 묶음 2 (`55a6c37` V9 마이그레이션, 2026-05-15) — `UPDATE access_logs SET auth_result = 'APPROVED' WHERE auth_result = 'ALLOW'` 1회 실행. curl 검증: `GET /access-logs?authResult=ALLOW` totalElements=0 (이전 8) / `GET /access-logs?authResult=APPROVED` totalElements=61 (이전 53+8). web 측 후속: `features/accesslog/types.ts` ALLOW literal 호환 제거 권장 (web/SUGGESTIONS.md #2 append).

---

## 9. (중) POWER 미터 보유 zone 목록 엔드포인트 추가 — `GET /api/v1/power/zones`

**발생 맥락**
- `SmartOffice-web` 플랜 3-1 G2 단계 `PowerCurrentWidget` 시각 검증 중 회의실 A·B 의 실시간 전력이 표시되지 않는 결함을 발견 (2026-05-14).
- 원인은 web 측 설계 결함이었음. 위젯이 `IntegratedDashboard` 의 환경 모니터링 셀렉터(selectedZoneId)를 그대로 공유하는데, 그 셀렉터는 `GET /api/v1/dashboard/sensors/current` 응답의 zoneId 만 노출 → 환경 센서(TEMPERATURE/HUMIDITY/CO2)를 보유하지 않은 zone(POWER 미터만 보유한 회의실 A·B)은 셀렉터에서 선택 자체가 불가능.
- 백엔드 데이터는 정상 (V7 시드에 zone 2/4/5/7 POWER 미터 시드 존재).

**현재 임시 처리 (web 측)**
- `SmartOffice-web/src/features/power/constants.ts` 신규 — `POWER_ZONES_TEMP` 상수에 V7 시드 기준 zone 4건 하드코딩 (회의실 A · 회의실 B · 개발팀 좌석 · 서버실).
- `PowerCurrentWidget` 가 환경 셀렉터와 분리된 **자체 셀렉터** 도입.
- 한계: zone 추가/삭제 시 프론트 상수를 수동 갱신해야 함. 시드 ↔ 상수 동기화 부담.

**제안**
- 신규 엔드포인트: `GET /api/v1/power/zones`
- 응답 DTO 예시:
  ```json
  {
    "code": "success",
    "message": "정상 조회되었습니다.",
    "data": [
      { "zoneId": 2, "zoneName": "회의실 A", "meterCount": 1 },
      { "zoneId": 4, "zoneName": "회의실 B", "meterCount": 1 },
      { "zoneId": 5, "zoneName": "개발팀 좌석", "meterCount": 1 },
      { "zoneId": 7, "zoneName": "서버실", "meterCount": 1 }
    ]
  }
  ```
- 구현 메모: `sensor_logs` 또는 `devices` 에서 `sensor_type='POWER'` 또는 `device_type='POWER_METER'` 가 등록된 zone 을 distinct 로 집계. (현재 시드는 sensor_logs.sensor_type='POWER' 사용.)
- 권한: `@PreAuthorize("hasRole('ADMIN')")` (대시보드 위젯용).

**채택 시 web 후속 작업**
- `constants.ts` 의 `POWER_ZONES_TEMP` 제거 → `usePowerZones()` 훅으로 전환.
- `PowerCurrentWidget` 의 Select 옵션 소스를 hook 응답으로 변경.

**우선순위**: 중. 현재 임시 하드코딩으로 동작 가능하나, zone 변경 시 web 수동 동기화 부담 + 시드 기반 가정이 운영 환경과 어긋날 가능성 있음.

**출처 세션**: `SmartOffice-web` 플랜 3-1 G2 `PowerCurrentWidget` 결함 분석 — 옵션 4(자체 셀렉터 + 임시 하드코딩 + BACKEND_SUGGESTIONS) 채택 (2026-05-14).

---

## 10. (저~중) `GET /api/v1/zones/{id}` 신설 권장

**근거**
- `SmartOffice-web` 플랜 3-2 G5 `ZoneDetailView` 구현 중 발견. `ZoneController` 5종에 `GET /{id}` 부재 (`GET 목록 / GET /tree / POST / PUT / DELETE` 만 존재). `ZoneDetailResponse` DTO 자체 부재.
- 다른 도메인(`device`, `asset`, `user`, `nfccard`)은 모두 `GET /{id}` 보유 — REST 컨벤션 정합성 누락.
- zone N건 전체 조회 후 클라이언트 `find(id)` 방식은 V7 시드 ~10건 규모에서는 무시 가능하나, zone 100건+ 시점에 비효율.
- 향후 zone detail 전용 집계 필드(설치 장치 수, 자식 zone 수, 진행 중 예약 수 등) 추가 시 별도 엔드포인트가 자연스러움.

**임시 처리 (web 단독, 채택 대기)**
- `SmartOffice-web/src/features/zone/hooks.ts:useZoneDetail(id)` 가 `useZones()` 응답을 `useMemo` + `find(z => z.id === id)` 로 추출.
- `ZoneListItemResponse = { id, name, zoneType, parentId, description, createdAt }` 6 필드가 `ZoneDetailView` 표시 필드를 100% 충족함을 검증 완료 (`ZoneInfoTab` 표시 필드 기준).
- `zoneKeys.detail(id)` queryKey 는 백엔드 채택 대비 유지 — hook 내부만 `useQuery` 로 swap 가능.

**제안 범위**
- 컨트롤러: `ZoneController` 에 `GET /api/v1/zones/{id}` 추가, `@PreAuthorize("hasRole('ADMIN')")`.
- DTO: `ZoneListItemResponse` 동일 DTO 재사용 가능. 또는 확장 `ZoneDetailResponse` (`childCount`, `deviceCount`, `activeReservationCount` 등 detail 전용 집계 포함) 신설.
- 서비스: `ZoneService.getZoneDetail(Long id)` 도입. 존재하지 않으면 `CustomException(ErrorCode.ZONE_NOT_FOUND)`.
- 응답 예 (`ZoneListItemResponse` 재사용 시):
  ```json
  {
    "code": "success",
    "message": "정상 조회되었습니다.",
    "data": {
      "id": 2,
      "name": "회의실 A",
      "zoneType": "MEETING_ROOM",
      "parentId": 1,
      "description": "3F 회의실",
      "createdAt": "2026-03-02T09:00:00"
    }
  }
  ```

**채택 시 web 후속 작업**
- `features/zone/api.ts` 에 `getZoneDetail(id)` 추가.
- `features/zone/hooks.ts:useZoneDetail(id)` 내부를 `useQuery({ queryKey: zoneKeys.detail(id), queryFn: () => zoneApi.getZoneDetail(id) })` 로 전환. queryKey 그대로 유지하므로 컴포넌트 변경 없음.

**우선순위**: 저~중. 현재 동작 가능하나, zone 증가 또는 detail 전용 필드 확장 시점에 전환 필요.

**출처 세션**: `SmartOffice-web` 플랜 3-2 0단계 read-only 검증 (차이 #1) — `ZoneListItemResponse` DTO 충족성 검증 완료 (2026-05-14).

---

## 11. (상) `GET /api/v1/power/zones/{zoneId}/hourly` HTTP 500 결함

**근거**
- `SmartOffice-web` 플랜 3-2 묶음 4 종료 백엔드 curl 검증 중 발견 (2026-05-14).
- POWER 미터 보유 zone (V8 시드 기준 2 · 4 · 5 · 7) **모두 일관 HTTP 500** 반환:
  ```
  {"code":"error","data":null,"message":"서버 내부 오류가 발생했습니다."}
  ```
- `startDate`/`endDate` 파라미터 유무와 무관 (없어도 500).
- 동 컨트롤러의 `/billing` 엔드포인트는 정상 (`GET /power/zones/5/billing?year=2026&month=4` → `totalKwh 2350.50` 정상 응답) — service layer 의 `getHourlyHistory` 만 실패.

**추정 원인 (백엔드 read-only 분석)**
- `PowerController.getHourlyHistory` → `PowerService.getHourlyHistory(zoneId, startDate, endDate, deviceId)` 호출.
- `HourlyPowerProjection` interface (`String getHourAt()` + 다수 `BigDecimal`) → repository projection mapping NPE 가능성.
- 또는 startDate=null 처리 누락 (default 처리 없으면 NPE).
- 또는 sensor_logs hourly aggregation native query 의 group-by 결함.

**영향**
- `SmartOffice-web` G7 `PowerHourlyChart` (묶음 4 커밋 `35cdef7`) + G5 `ZonePowerTab` (커밋 `20e50cd`).
- web 측은 `usePowerHourly` 의 `isError` 분기 + `ErrorBoundary` 로 graceful handling — 앱 정상 동작, **시각화만 비활성**. UI 회귀 없음.

**제안 범위**
- `PowerService.getHourlyHistory` 디버깅:
  - `HourlyPowerProjection` mapping (native query / @Query 의 alias 일치 확인)
  - `startDate`/`endDate` null 시 default 처리 (예: 최근 24h)
  - sensor_logs 의 sensor_type='POWER' 데이터 존재 확인
- repository 단위 테스트 추가 (V8 시드 기반 POWER zone 4건).

**우선순위**: 상. 본 플랜의 `PowerHourlyChart` 시각화 핵심 기능이 비활성 상태. 시연 직전 결함.

**출처 세션**: `SmartOffice-web` 플랜 3-2 묶음 4 종료 curl 검증 (2026-05-14).

**처리 완료**: 백엔드 수정 sprint 묶음 1 (`bfe28be` fix + `4a8d744` test, 2026-05-15) — 마스터플랜 가설 1 (interface projection snake_case alias 매핑) 은 결함 아니었음. 실제 원인은 `findHourlyPowerProjection` nativeQuery 의 SELECT `DATE_FORMAT(sl.logged_at, '%Y-%m-%dT%H:00:00') AS hour_at` ↔ GROUP BY `DATE_FORMAT(sl.logged_at, '%Y-%m-%d %H')` 패턴 불일치 → MySQL `only_full_group_by` 모드에서 Expression #4 비집계 컬럼 거부 (SQLSyntaxErrorException). GROUP BY 패턴을 SELECT 와 정확히 동일한 `'%Y-%m-%dT%H:00:00'` 로 통일. 클라이언트 fmt parser 영향 0. curl `GET /power/zones/5/hourly` 200 + `logs[]` 9건 정상 (zone 5 개발팀 좌석, device 9 개발팀 전력미터, hourAt 패턴 `yyyy-MM-ddTHH:00:00`). web `/building` 시간별 전력 사용량 + `/zones` 전력 탭 복원 시각 검증 통과. `PowerServiceIntegrationTest` 신설 (Testcontainers MySQL + V7/V8 시드, zone 5/2 hourly + ZONE_NOT_FOUND 3 시나리오).

---

## 12. (중) `control_commands.command_type` 정의 부재 — enum 또는 메타 엔드포인트 권장

**근거**
- `ControlCommand.commandType` 컬럼은 `varchar(15)` 자유 string 이며 검증 enum/허용 목록 부재 (`ControlService.sendCommand` 가 `request.getCommand()` 를 그대로 저장 + MQTT 발송).
- 어떤 command 값이 IoT(RPi) 에서 처리 가능한지가 **백엔드·web·IoT 어디에도 정의되지 않음**.
- V8 시드 history 실제 값: `AC` · `LIGHT` · `FAN` (총 17건). `SmartOffice-web/src/components/building/ControlPanel.tsx` 의 quick command 초안은 `LIGHT_ON` · `LIGHT_OFF` · `SET_TEMPERATURE` · `DOOR_LOCK` 으로 시드와 어긋남 → V8 정합 `AC` · `LIGHT` · `FAN` · `DOOR_LOCK` 로 web 측 정정 완료 (`SmartOffice-web` `fix(control)` 커밋).
- 본질 결함: web 하드코딩이 IoT 가 실제 이해하는 명령과 일치하는지 검증 불가. 컨벤션이 백엔드에 없음.

**제안 범위 (택일)**
- **(A) `ControlCommandType` enum + 검증 (간단, 권장)**:
  - `domain/control/entity/ControlCommandType` enum 정의 (예: `AC | LIGHT | FAN | DOOR_LOCK | SET_TEMPERATURE`).
  - `ControlCommand.commandType` 컬럼 타입 `varchar(15)` → `@Enumerated(EnumType.STRING)` (또는 string 유지하되 service 에서 enum 변환 검증).
  - `ControlRequest.command` 를 enum 타입으로 받거나 service 에서 `ControlCommandType.valueOf(req.getCommand())` 변환 + `CustomException(INVALID_COMMAND_TYPE)`.
- **(B) `GET /api/v1/controls/commands` 메타 엔드포인트**:
  - 응답 예: `[{"command":"AC","label":"공조 가동","valueType":"NUMERIC","unit":"°C"}, {"command":"LIGHT","label":"조명","valueType":"BOOLEAN"}, ...]`
  - web/모바일이 동적으로 quick command 버튼 생성 → IoT 컨벤션 변경 시 backend 1곳만 수정.
- (A) + (B) 조합 가능: enum 정의 + 메타 엔드포인트가 enum 을 reflect.

**채택 시 web 후속 작업**
- `ControlPanel.QUICK_COMMANDS` 하드코딩 제거 → `useControlCommands()` hook 으로 전환 (또는 enum import).
- 모바일 앱(`SmartOffice-app`) 도 동일 hook/enum 재사용.

**우선순위**: 중. 현재 web 하드코딩으로 동작 가능하나, IoT 측 컨벤션 변경 또는 신규 명령 추가 시 web 수동 동기화 부담 + 잘못된 string 발송 가능성. 시연 후 정리 권장.

**출처 세션**: `SmartOffice-web` 플랜 3-2 시각 검증 후속 (ControlPanel 명령 종류 검토, 2026-05-14).

---

## [2026-05-14] (상) PUT /api/v1/zones/{id} body deserialize 결함 — "요청 본문을 읽을 수 없습니다"

**발생 맥락**

- `SmartOffice-web` 플랜 3-2 묶음 6 (시각 재검증 후속 fix 라운드 2차) 중 ZoneInfoTab 의 zone 수정 모달이 모달 자체는 정상 렌더되나 저장 시 백엔드가 일관되게 에러 응답.
- 시연 환경 admin 토큰으로 curl 직접 검증:
  - `PUT /api/v1/zones/2` body `{"name":"회의실 A (수정)","zoneType":"ROOM","description":"테스트"}` → `{"code":"error","message":"요청 본문을 읽을 수 없습니다"}`
  - body 에 `parentId` · `clearParent` 포함한 변형도 동일 에러
  - `name` 만 보낸 최소 body 도 동일 에러
  - 참고: `POST /api/v1/zones` (동일/유사 body) 는 정상 200 응답 — 결함은 PUT 한정.
- 사용자 시각 보고: "zone 이름 수정 후 사라짐" → 모달이 mutation 성공 시 닫히지만 invalidate 후 목록 갱신 결과가 원본 그대로 (수정 미반영).

**추정 원인**

- `ZoneUpdateRequest` DTO 의 `clearParent` 필드가 **primitive `boolean`** 으로 선언되어 있고 `@NoArgsConstructor` 환경에서 setter 가 부재한 것으로 추정.
- Jackson 이 PUT body 를 deserialize 할 때, primitive `boolean` 필드는 기본값 `false` 가 있으나 setter 없이는 주입 경로가 없어 `JsonMappingException` 발생 가능.
- POST 가 정상이라는 점은 `ZoneCreateRequest` 와 `ZoneUpdateRequest` 의 필드 구성 차이 (특히 `clearParent` 의 존재 여부) 가 결정적 차이임을 시사.
- 백엔드 코드 read-only 검증 미실시 (web 작업자 권한 밖). 위 추정은 web 측 curl 응답 메시지와 DTO 비교로부터 유추된 가설이며, 백엔드 작업자가 원인 확인 필요.

**제안**

- (택일 또는 조합)
  - **(A)** `ZoneUpdateRequest.clearParent` 를 `Boolean` (wrapper) 으로 변경 + `@JsonProperty` 명시 + Lombok `@Setter` 또는 `@Data` 적용 확인.
  - **(B)** Lombok `@Builder` / `@AllArgsConstructor` / `@Jacksonized` 조합으로 record-like 불변 DTO 로 재설계 (스프링 기본 `MappingJackson2HttpMessageConverter` 와 호환).
  - **(C)** Controller 메서드에 `@RequestBody` 가 누락되었거나 `consumes = "application/json"` 미설정 등 사소한 메타 이슈 확인 (가능성 낮음, POST 가 정상이므로).
- 수정 후 통합 테스트: `ZoneControllerTest` 에 PUT 케이스 추가 — `name` 만 / `parentId` 만 / `clearParent=true` / 전체 필드 4가지 body 변형이 200 으로 응답해야 한다.

**web 영향**

- web 측 우회 불가 (deserialize 가 컨트롤러 진입 전에 실패하므로 응답 메시지·재시도 모두 불가능).
- 현재 web ZoneInfoTab 수정 모달은 mutation 성공 후 닫히지만 실제 데이터는 변경되지 않음 → 시연 시 "수정이 적용되지 않음" 회귀 노출.
- 백엔드 수정 후 web 측 추가 작업 불필요 (request body shape 변경 없음 가정).

**우선순위**: **상** — zone 수정이 admin 운영 기능의 핵심 액션 중 하나이며, 현재 완전 차단 상태. 시연 영향도 큼.

**출처 세션**: `SmartOffice-web` 플랜 3-2 묶음 6 시각 재검증 (Fix 7, 2026-05-14).

**처리 완료**: PR #27 (`a32d146`, 2026-05-15) — `ZoneUpdateRequest.clearParent` Boolean wrapper + `@Setter` + `ZoneServiceImpl` null safe + `Zone.update()` partial update null 가드. ZoneControllerTest PUT 5 시나리오 + ZoneInfoTab 시각 검증 통과. clearParent flag 의미 정정은 별도 sprint (`SUGGESTIONS.md` #17 분리 등록).

---

## [2026-05-14] (저~중) 주차 차량(Vehicle)/예약(Reservation) 모델 신설 검토

**발생 맥락**

- `SmartOffice-web` 플랜 3-3 G9 ParkingManagement 흡수 중 발견.
- 현재 `ParkingController` 가 제공하는 기능 = `ParkingSpot` CRUD (cat 2 ADMIN) + zone summary/map (cat 5 인증) + IoT 점유 상태 업데이트 (cat 4 permitAll).
- 백엔드 모델에 부재한 운영 기능 (mock UI 가 표현하고 있던 항목):
  - 차량(Vehicle) — 번호판, 소유자, 임직원/방문객 구분, 방문 목적
  - 차량 예약(ParkingReservation) — 사전 예약, 입출차 시각, 상태(RESERVED/PARKED/EXITED)
- 현재 `ParkingSpot` 만으로는 "누가 점유했는지" 추적 불가 (IoT 센서는 점유 여부만 갱신, identity 없음).

**임시 처리 (web)**

- `SmartOffice-web/src/components/dashboard/ParkingManagement.tsx` 의 차량 의존 코드 전체 제거 (`vehicles` mock · 등록 Dialog · 상세·수정 Dialog · 상태 변경 dropdown · filterStatus · 차량 stats 카드 3종).
- 잔존 UI = ParkingSpot CRUD + zone summary + zone map(평면도). Stat 카드는 spot 통계(`총 주차면 / 점유 / 여유 / 비활성`) 로 재구성.
- 차량 관리 기능은 본 plan 범위 외로 격리.

**제안 범위 (택일 또는 조합)**

- **(A) Vehicle + ParkingReservation 신설 (단순 대장 모델)**:
  - `Vehicle` (plate_number, owner_name, owner_user_id?, type[STAFF|VISITOR], purpose?, created_at)
  - `ParkingReservation` (vehicle_id, zone_id, spot_id?, reserved_at, entry_at?, exit_at?, status[RESERVED|PARKED|EXITED])
  - 컨트롤러: `POST/GET/PUT/DELETE /api/v1/vehicles`, `POST/GET/PUT/DELETE /api/v1/parking/reservations`.
- **(B) AccessLog 통합 (NFC/번호판 인식)**:
  - 출입 시스템(access_log) 에 차량 entry 를 동등 처리 — `access_logs.vehicle_plate?` + `access_logs.entry_type[USER|VEHICLE]`.
  - ALPR(차량 번호판 인식) 또는 RFID 태그 연계 (별도 IoT 설계).
- **(C) 별도 ALPR 서비스 연계**:
  - 외부 ALPR 카메라/서비스 (예: Hikvision, Genetec) 통합. 별도 마이크로서비스로 분리.

**근거**

- mock UI 의 차량 등록/예약/입출차 흐름이 시연 환경에서 의미 있는 운영 요구로 보임 (방문객 차량 사전 등록, 임직원 차량 입출차 시각 기록 등).
- 현재 ParkingSpot 만으로는 점유 여부만 알 수 있고, 누구의 차량인지 / 언제 들어왔는지 추적 불가.
- 옵션 (A) 가 가장 직접적이지만, 향후 NFC/출입 시스템과 통합 가능성 고려 시 (B) 도 매력적. 설계 결정 선행 필요.

**채택 시 web 후속 작업**

- 옵션 (A) 채택 시: `src/features/vehicle/{...}` + `src/features/reservation/{...}` 신설. `ParkingManagement` 에 차량 등록/상세/예약 UI 복원 (기존 mock 패턴 재활용 가능).
- 옵션 (B) 채택 시: `src/features/accesslog/` 확장 — 차량 출입 표시. ParkingManagement 는 spot 중심 유지.

**우선순위**: 저~중 — 단순 차량 대장 vs 출입 시스템 통합 vs 별도 ALPR 연계 (설계 결정 선행). 현재 web 측은 spot 관리만으로 동작 가능. 시연 후 정리 권장.

**출처 세션**: `SmartOffice-web` 플랜 3-3 0단계 검증 (차이 C, 2026-05-14).

---

## [2026-05-14] (저~중) GET /api/v1/parking/zones — 주차면 보유 zone 목록 조회 엔드포인트 신설

**발생 맥락**: `SmartOffice-web` 플랜 3-3 시각 검증에서 발견. `ParkingManagement` 헤더 zone Select 가 `useZones()` 전체 14건(FLOOR 5 + AREA 4 + ROOM 5)을 노출하여 주차면 0건 zone(회의실 C/D/E·휴게실·카페 라운지 등) 선택 시 빈 응답 — UX 무의미. 사용자 의문: 주차면 보유 zone(현 시드 8/9 = 지하1층/지하 2층)만 떠야 하지 않나?

**임시 처리(web 단독)**: `useParkingSpots({})` 응답 zoneId distinct + `useZones()` name 매핑으로 자체 셀렉터 구성. POWER_ZONES_TEMP / `useZoneDetail`(GET /zones/{id} 부재 우회) 패턴 동일. 시드 보유 zone만 동적으로 노출되어 UX 정합 즉시 달성.

**제안 (옵션 A — 권장)**: `GET /api/v1/parking/zones` 신설.

```
GET /api/v1/parking/zones
→ List<ParkingZoneListResponse>
  { zoneId, zoneName, zoneType, totalSpots, occupiedSpots }
```

- 주차면(`parking_spots`)이 1건 이상 등록된 zone 만 반환.
- 응답에 `totalSpots`·`occupiedSpots` 포함 시 ParkingManagement 헤더에서 Select trigger label 에 보조 정보 노출 가능 ("지하1층 (15면)" 등).
- 권한: ADMIN (조회). 평균 응답 zone 수 적음(현 시드 2건, 운영 ≤10건 예상) → 페이지네이션 불필요.

**제안 (옵션 B)**: 기존 `GET /api/v1/parking/spots` 응답에 `zoneId` 만 distinct 추출하는 클라이언트 책임 유지. 별도 엔드포인트 신설 X.

- 단점: 모든 spot row 를 받아 distinct — N+1 비효율(spot 수 ≫ zone 수). 현 시드 25건 수준이라 무시 가능하나 대규모 시 부담.

**채택 시 web 후속 작업**

- 옵션 A 채택 시: `src/features/parking/hooks.ts` 에 `useParkingZones()` 추가, `ParkingManagement.tsx` 의 `allSpotsForZonesQuery` + `useMemo` distinct 로직을 `useParkingZones()` 단일 호출로 교체. queryKey 는 `parkingKeys.zones` 신규.
- 옵션 B 채택 시: 현 web 우회 그대로 유지(추가 작업 없음).

**근거**

- 동일 패턴이 이미 web 우회로 검증됨. 백엔드 측은 단순 `JPQL SELECT DISTINCT z FROM Zone z JOIN ParkingSpot p ON p.zone = z` 1쿼리로 구현 가능.
- 응답에 통계(`totalSpots`·`occupiedSpots`) 포함 시 cat 5 zone summary 엔드포인트(`/parking/zones/{zoneId}/spots`)와 책임 분담 명확 — 목록용 가벼운 응답 vs 상세 요약.

**우선순위**: 저~중 — 현재 web 우회 동작 가능. 백엔드 부하·UX 정합도 측면에서 채택 권장. dashboard/sensors-current 와 유사한 도메인별 zone 목록 패턴.

**출처 세션**: `SmartOffice-web` 플랜 3-3 시각 검증 결함 12-1 (2026-05-14).

---

## [2026-05-14] (중) parking_spots 좌표 충돌 검증 — UNIQUE 제약 + null XOR 가드

**발생 맥락**: `SmartOffice-web` 플랜 3-3 시각 재검증 중 발견. `ParkingZoneMap` 좌표 스케일링 fix 후 평면도 격자 렌더는 정상이나, 백엔드 측 `(zone_id, position_x, position_y)` 유니크 검증 부재로 같은 zone 같은 좌표에 spot 여러 개 등록 가능. 평면도에서 spot 타일이 z-stack 으로 겹쳐 마지막 등록만 노출됨 — 결함 11 root cause 와 동일한 시각 결함이 데이터 무결성 차원에서 재현 가능.

**현황**:

- DB `parking_spots` 유니크 제약 (V4): `(zone_id, spot_number)` + `device_id` 단독. **`(zone_id, position_x, position_y)` 부재**
- `ParkingServiceImpl.createSpot` / `updateSpot`: `spot_number`, `device_id` 충돌 검증만 존재. **좌표 충돌 검증 X**
- `position_x` / `position_y` 둘 다 nullable — **null XOR (한쪽만 null) 검증 부재**. 좌표 시스템 의미상 "둘 다 null (grid fallback)" 또는 "둘 다 값" 만 정합

**임시 처리(web 단독)**: `ParkingSpotsTable` 등록/수정 핸들러에 사전 검증 추가. `useParkingSpots({})` 응답에서 같은 zone + 같은 좌표 spot 검색 → 충돌 시 toast 차단. null XOR 도 동일 가드. 백엔드 채택 시 web 사전 검증 제거 (단, race condition 보장은 서버 측에서만 가능).

**제안**

1. **DB UNIQUE 인덱스 추가** (Flyway V9 migration):

```sql
ALTER TABLE parking_spots
  ADD CONSTRAINT UQ_PARKING_SPOTS_POSITION UNIQUE (zone_id, position_x, position_y);
```

- MySQL `UNIQUE` 의 NULL ≠ NULL 정책으로 좌표 둘 다 null 인 spot 은 자유 통과 (grid fallback 자연 보존)
- 기존 시드(V8) 는 zone 8/9 좌표 모두 (1~10, 1~2) 분포로 충돌 없음 — migration 안전

2. **Service 충돌 검증** (`ParkingServiceImpl`):

- `createSpot`: 좌표 둘 다 not null 시 `parkingSpotRepository.existsByZone_ZoneIdAndPositionXAndPositionY(zoneId, x, y)` → 충돌 시 신규 `ErrorCode.DUPLICATE_SPOT_POSITION`
- `updateSpot`: `existsByZone_ZoneIdAndPositionXAndPositionYAndSpotIdNot(...)` (자기 자신 제외)

3. **null XOR 가드** (`ParkingSpotCreateRequest` / `UpdateRequest`):

- bean validation custom annotation 또는 service 진입 직후 명시 검증
- `(positionX == null) != (positionY == null)` 차단 → 신규 `ErrorCode.INVALID_POSITION_PAIR` 또는 기존 `INVALID_INPUT` 재사용

**근거**

- 좌표는 평면도 시각 정합의 핵심 키. 충돌 시 z-stack 으로 마지막 spot 만 표시 → 결함 11 fix 후에도 동일 증상 가능
- web 사전 검증은 UX 즉시 개선이나 race condition (동시 등록 2건) 보장 X. 데이터 무결성은 DB 제약으로만 가능
- `spot_number` 와 `device_id` 가 이미 유니크 제약 보유 → 좌표도 동일 정책으로 일관성 유지

**채택 시 web 후속 작업**

- `ParkingSpotsTable` 사전 검증 로직 제거 (백엔드 4xx + `getErrorMessage` 토스트로 충분)
- 신규 ErrorCode 추가 시 `src/lib/api/errors.ts` 한국어 매핑 갱신 (`DUPLICATE_SPOT_POSITION` → "이미 등록된 좌표입니다." 등)

**우선순위**: 중 — 데이터 무결성 + UX 정합. 시연 환경에서 발견 가능한 결함이라 우선 처리 권장. `spot_number`/`device` 유니크와 동일 레벨.

**출처 세션**: `SmartOffice-web` 플랜 3-3 시각 재검증 좌표 충돌 진단 (2026-05-14).

**처리 완료**: 백엔드 수정 sprint 묶음 2 (`7975c49` fix + `3101713` test, 2026-05-15) — V10 `ALTER TABLE parking_spots ADD CONSTRAINT UQ_PARKING_SPOTS_POSITION UNIQUE (zone_id, position_x, position_y)` + `ParkingServiceImpl.validatePosition` 헬퍼 (null XOR 가드 + 좌표 충돌 검증) → createSpot/updateSpot 양쪽 적용 + ErrorCode 신규 2건 (`DUPLICATE_SPOT_POSITION` 409 / `INVALID_POSITION_PAIR` 400). MySQL UNIQUE 의 NULL ≠ NULL 정책으로 grid fallback (좌표 둘 다 null) 자유 통과. ParkingServiceIntegrationTest 신규 3 시나리오 (null XOR / 좌표 충돌 / grid fallback) 통과. curl 검증: POST `/parking/spots` (zone 8, x=6, y=1 = B1-006 동일 좌표) → 409 "동일 구역 내 중복된 주차면 좌표입니다.", `{positionX:99}` only → 400 "주차면 좌표는 X/Y 둘 다 입력하거나 둘 다 비워야 합니다.". web 측 후속: `ParkingSpotsTable` 사전 검증 로직 제거 권장.

---

## [2026-05-15] (중) Zone partial update — clearParent flag 의미를 Zone.update()에 정확 반영

**발생 맥락**: 백엔드 수정 sprint 플랜 1 #13 (PUT /zones/{id} body deserialize fix) 0단계 진단 중 발견. `Zone.update()` (`domain/zone/entity/Zone.java:61`) 의 `parent` / `zoneDescription` 필드가 무가드로 덮어쓰여 부분 수정 시 데이터 손실 발생 가능성 노출 — `{"name":"새이름"}` body 만 보내면 기존 parent 와 description 이 모두 NULL 로 갱신됨. 시연 데이터 손실 방지 차원에서 본 PR 에서 null 가드만 추가하여 우회했으나, 그 결과 `clearParent=true` (의도적 parent 제거) 시나리오가 무력화됨.

**현황**:

- `Zone.update()` 수정 후: `if (parent != null) this.parent = parent;` / `if (zoneDescription != null) this.zoneDescription = zoneDescription;` (#13 PR 적용)
- `ZoneServiceImpl.updateZone` (`domain/zone/service/ZoneServiceImpl.java:64`): `Boolean.TRUE.equals(request.getClearParent())` 결과를 **`currentParentId` 계산 (중복 이름 검증용) 에만 사용**. `Zone.update()` 호출 시 `newParent` 인자에는 반영하지 않음
- 결과: `{"clearParent": true}` body 전송 시 200 응답이지만 DB 의 `zone_parent_id` 는 변경되지 않음 (기존 parent 보존)

**clearParent flag 의 의도된 의미**

- `clearParent=true`: 상위 구역 명시적 제거 (parent → null 로 강제 변경)
- `clearParent=false` 또는 null: parentId 변경 의사 없음 (기존 parent 유지)
- `parentId` 가 명시된 경우 (값 또는 null): clearParent 와 무관하게 해당 값으로 변경

**제안 범위 (택일)**

- **(A) `Zone.update()` 시그니처에 `clearParent` boolean 인자 추가 (간단)**:
  - `zone.update(name, type, parent, description, clearParent)` — `if (clearParent || parent != null) this.parent = parent;`
  - `ZoneServiceImpl` 호출 측에서 flag 전달
- **(B) `Zone` 에 별도 메서드 분리 (의도 명확)**:
  - `updateMeta(name, type, description)` + `updateParent(Zone parent)` + `clearParent()` 3 메서드
  - `ZoneServiceImpl` 가 request 의 의도(name 만 / parent 변경 / parent 제거) 에 따라 메서드 선택 호출
- **(C) `ZoneServiceImpl` 가 `clearParent=true` 시 별도 처리 (가장 작은 변경)**:
  - 기존 `zone.update(...)` 호출 후 `Boolean.TRUE.equals(request.getClearParent())` 시 `zone.clearParent()` 추가 호출
  - `Zone` 에 `clearParent()` 메서드 (단순 `this.parent = null`) 신규 추가

**근거**

- 시연 환경의 zone 수정 모달은 현재 "parent 변경" 시나리오 위주이며 "parent 명시적 제거" 호출은 web 측 미확인 (현재 ZoneInfoTab 의 parent 변경 UI 자체가 미완성). 하지만 clearParent flag 자체가 DTO 에 존재하므로 의도된 사용처가 향후 개발될 가능성
- (C) 옵션이 가장 작은 변경 — `Zone.update()` 시그니처 유지 + 호출 측 1 줄 추가
- 단위 테스트: `ZoneServiceTest` 에 `updateZone_clearParentTrue_setsParentNull` / `updateZone_clearParentFalse_preservesParent` 2 케이스 추가 권장

**채택 시 web 후속 작업**

- 옵션 어느 것이든 web request body shape 변화 없음 (`clearParent` 필드 그대로 활용). web 측 추가 작업 불필요
- ZoneInfoTab 에 "최상위로 변경" 버튼/체크박스 신규 추가 가능 (현재는 parent 변경 UI 자체가 미구현)

**우선순위**: 중 — 현재 동작 가능 (#13 null 가드로 시연 데이터 손실 방지 완료) + clearParent 시나리오 사용처 web 측 미확인. 의미 정정은 후속 sprint 권장. `Zone` 도메인 모델 무결성 차원에서는 처리 필요.

**출처 세션**: 백엔드 수정 sprint 플랜 1 #13 0단계 진단 (2026-05-15).

---

## 검증 노트 (2026-05-14)

본 통합 작업의 인증 도메인 마이그레이션 시점에 로컬 백엔드(`./gradlew bootRun` · 8080) 응답을 curl 로 직접 검증했다.

- `GET /actuator/health` → `{"status":"UP", ...}`
- `POST /api/v1/auth/login` (admin@grown.com / EMP001) → `ApiResponse.success` + `LoginResponse` (필드 6개) 모두 일치
- `GET /api/v1/auth/me` (Bearer 첨부) → `MeResponse` 필드 10개 모두 일치

따라서 `SmartOffice-web/src/features/auth/types.ts` 는 백엔드 DTO 와 정확히 정합한 상태에서 마이그레이션을 완료했다. 후속 도메인(users, departments, attendance, ...) 마이그레이션 시에도 동일한 1:1 검증 절차를 따른다.
