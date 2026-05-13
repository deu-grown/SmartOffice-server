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

## 검증 노트 (2026-05-14)

본 통합 작업의 인증 도메인 마이그레이션 시점에 로컬 백엔드(`./gradlew bootRun` · 8080) 응답을 curl 로 직접 검증했다.

- `GET /actuator/health` → `{"status":"UP", ...}`
- `POST /api/v1/auth/login` (admin@grown.com / EMP001) → `ApiResponse.success` + `LoginResponse` (필드 6개) 모두 일치
- `GET /api/v1/auth/me` (Bearer 첨부) → `MeResponse` 필드 10개 모두 일치

따라서 `SmartOffice-web/src/features/auth/types.ts` 는 백엔드 DTO 와 정확히 정합한 상태에서 마이그레이션을 완료했다. 후속 도메인(users, departments, attendance, ...) 마이그레이션 시에도 동일한 1:1 검증 절차를 따른다.
