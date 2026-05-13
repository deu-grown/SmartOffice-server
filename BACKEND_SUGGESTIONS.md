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

## 검증 노트 (2026-05-14)

본 통합 작업의 인증 도메인 마이그레이션 시점에 로컬 백엔드(`./gradlew bootRun` · 8080) 응답을 curl 로 직접 검증했다.

- `GET /actuator/health` → `{"status":"UP", ...}`
- `POST /api/v1/auth/login` (admin@grown.com / EMP001) → `ApiResponse.success` + `LoginResponse` (필드 6개) 모두 일치
- `GET /api/v1/auth/me` (Bearer 첨부) → `MeResponse` 필드 10개 모두 일치

따라서 `SmartOffice-web/src/features/auth/types.ts` 는 백엔드 DTO 와 정확히 정합한 상태에서 마이그레이션을 완료했다. 후속 도메인(users, departments, attendance, ...) 마이그레이션 시에도 동일한 1:1 검증 절차를 따른다.
