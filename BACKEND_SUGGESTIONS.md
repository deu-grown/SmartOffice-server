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

---

## 검증 노트 (2026-05-14)

본 통합 작업의 인증 도메인 마이그레이션 시점에 로컬 백엔드(`./gradlew bootRun` · 8080) 응답을 curl 로 직접 검증했다.

- `GET /actuator/health` → `{"status":"UP", ...}`
- `POST /api/v1/auth/login` (admin@grown.com / EMP001) → `ApiResponse.success` + `LoginResponse` (필드 6개) 모두 일치
- `GET /api/v1/auth/me` (Bearer 첨부) → `MeResponse` 필드 10개 모두 일치

따라서 `SmartOffice-web/src/features/auth/types.ts` 는 백엔드 DTO 와 정확히 정합한 상태에서 마이그레이션을 완료했다. 후속 도메인(users, departments, attendance, ...) 마이그레이션 시에도 동일한 1:1 검증 절차를 따른다.
