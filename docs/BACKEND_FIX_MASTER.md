# 백엔드 수정 마스터플랜

> 작성일: 2026-05-15 · 단일 sprint 정책 전환: 2026-05-15
> 상태: 단일 sprint 진입 대기 (#13 완료, 잔여 15건 묶음 단위 처리)
> 입력: `BACKEND_SUGGESTIONS.md` #1~#16 + `../SmartOffice-web/docs/PLAN_3_MASTER.md` 9절 잔존 결함 추적표 #1~#10
> **단일 sprint / 단일 PR / push 마지막 1회**
> 작업 브랜치: `feature/backend-fixes` (server 레포, main 기반, long-lived)
> 머지 정책: 본 sprint 전체 항목을 **단일 PR 1개**로 머지 (branch protection rule 준수, push 는 sprint 종료 시 1회)
> 다음 단계: 별도 plan mode 세션에서 묶음 1 (#7·#11) 진단 진입

본 문서는 SmartOffice-server 백엔드 수정 작업의 단일 진실 공급원이다. `BACKEND_SUGGESTIONS.md` 와의 책임 분담은 다음과 같다.

- **`BACKEND_SUGGESTIONS.md`** = 프론트 통합 작업에서 누적된 제안의 영구 보존 (출처 세션 · 우선순위 · 근거 기록). 처리 완료 시 본문에 "처리 완료" 표기 추가만 수행.
- **`BACKEND_FIX_MASTER.md`** (본 문서) = 실제 실행 계획. 단일 sprint 묶음 분할 · 작업 순서 · 진행 트래커 보유.

---

## 0. 컨텍스트

`SmartOffice-web` 통합 작업(2026-05-14~15) 종료 후, web 측은 모든 결함에 graceful handling(우회 상수 · `isError` + ErrorBoundary · 사전 검증 가드) 적용 완료하여 시연 동작은 가능하나 핵심 기능 일부가 영구 차단 상태이며, 데이터 정합 + 신설 엔드포인트 + 모델 신설 + 후순위 항목 등 누적 제안 16건(#1~#16, #13 완료)이 잔존한다.

**본 sprint 의 단일 sprint 전환 결정 (2026-05-15)**:

- 기존 플랜 1~4 분할 + 항목별 PR 구조 폐기
- **15건 단일 sprint** 로 일괄 처리 (#13 은 이미 PR #27 머지 완료)
- 단일 브랜치 `feature/backend-fixes` 에 **항목별 커밋 분리** + **단일 PR 1개** + **push 마지막 1회**
- 묶음(bundle) 단위로 **빌드/테스트 게이트 + BE+FE 로컬 시각 검증 게이트** 통과 후 다음 묶음 진입

---

## 1. 우선순위 그룹

- **상**: #7 · #11 · ~~#13(완료)~~ — 핵심 기능 차단, 시연 영향
- **중**: #8 · #9 · #10 · #12 · #15 · #16 (6건) — 기능 보조 또는 안전망
- **저~중**: #14 (1건) — Vehicle/ParkingReservation 모델 신설 (옵션 A 채택)
- **후순위(저)**: #1 · #2 · #3 · #4 · #5 · #6 (6건) — web 통합 전 누적된 제안

본 sprint 단일화 결정에 따라 우선순위 그룹은 작업 우선순위(묶음 진행 순서)로만 활용하며, 처리 자체는 잔여 15건 전수 진행한다.

---

## 2. 단일 sprint 작업 순서 (묶음 5개)

| 묶음 | 의미 | 항목 | 진입 조건 | 종료 조건 |
|------|------|------|-----------|-----------|
| **1** | 시연 차단 해소 | #7 + #11 | 본 sprint 시작 | 빌드/테스트 + 묶음 1 시각 검증 통과 |
| **2** | 스키마 변경 | #8 (V9) + #16 (V10) | 묶음 1 종료 | 묶음 2 시각 검증 통과 |
| **3** | 신설 엔드포인트 | #9 + #10 + #12 + #15 | 묶음 2 종료 | 묶음 3 시각 검증 통과 |
| **4** | 모델 신설 (#14, 옵션 A) | 4-a Vehicle + 4-b ParkingReservation | 묶음 3 종료 | 묶음 4 시각 검증 통과 |
| **5** | 후순위 | #1 + #2 + #3 + #4 + #5 + #6 | 묶음 4 종료 | 묶음 5 시각 검증 통과 → sprint 종료 |

각 묶음 종료 후 **사용자 BE+FE 로컬 시각 검증 게이트** (4절 프로토콜 참조) 통과 시 다음 묶음 진입. 검증 실패 시 묶음 내 추가 BE 수정 또는 web 결함 발견 시 `SmartOffice-web/SUGGESTIONS.md` append.

---

## 3. 묶음별 상세

### 3-1. 묶음 1 — 시연 차단 해소 (#7 + #11)

#### #7 GET /api/v1/dashboard/summary HTTP 500

**0단계 read-only 검증 결과 (2026-05-15)**:

`DashboardService.java:45-58` 확인:

- 4 카운트 모두 `(long → int)` 캐스팅 — `userRepository.countByStatus(UserStatus.ACTIVE)` · `deviceRepository.countByDeviceStatus("ACTIVE")` · `reservationRepository.countTodayConfirmed(ReservationStatus.CONFIRMED, startOfDay, endOfDay)` · `pendingApprovals=0` 하드코딩
- NPE 경로 없음. 진짜 원인 후보:
  - (a) `countByDeviceStatus(String)` — `DeviceRepository.java:19` 시그니처는 String. `Device.deviceStatus` 컬럼 타입이 enum 이면 단순 0 반환이라 무해. **확인 필요**.
  - (b) `countTodayConfirmed(ReservationStatus, LocalDateTime, LocalDateTime)` JPQL 쿼리 결함 가능.
  - (c) Spring Boot 4 / Hibernate 6.x 의 다른 쿼리 결함.

**처리 절차**: 1-A(진단) → 1-B(수정) 2단계 (동일 묶음 내 연속 처리).

- **1-A 진단**: `./gradlew bootRun` 기동 → admin 토큰 발급 → `curl -i .../dashboard/summary` → 콘솔 스택트레이스 확인 → 결함 메서드 식별
- **1-B 수정**: 결함 메서드 fix + `DashboardService.getSummary` 4 필드 정규화 + `DashboardServiceTest` 데이터 없음 케이스 단위 테스트 추가
- **커밋**: `fix(dashboard): GET /dashboard/summary 500 오류 해결` — 1-A 진단 결과를 커밋 본문 첫 줄에 명시

#### #11 GET /api/v1/power/zones/{zoneId}/hourly HTTP 500

**0단계 read-only 검증 결과 (2026-05-15)**:

`PowerService.java:44-75` + `SensorLogRepository.java:55-76` + `HourlyPowerProjection.java` 확인:

- Native query alias snake_case: `id` · `device_id` · `device_name` · `hour_at` · `kwh` · `avg_watt` · `peak_watt`
- Projection getter camelCase: `getId()` · `getDeviceId()` · `getDeviceName()` · `getHourAt()` · `getKwh()` · `getAvgWatt()` · `getPeakWatt()`
- **가설 1 (유력)**: Spring Boot 4 / Hibernate 6.x 의 nativeQuery + interface projection 매핑에서 snake_case alias → camelCase getter 자동 변환 미작동. `@Value("#{target.device_id}")` 명시 또는 alias 를 camelCase 로 변경 필요.
- 가설 2: `LocalDateTime.parse(p.getHourAt(), fmt)` 의 `DateTimeParseException` (가설 1 통과 시).
- `/billing` 엔드포인트는 entity 기반 응답이라 정상 작동 — 정합.

**처리 절차**: 1-A(진단) → 1-B(수정) 2단계.

- **1-A 진단**: `curl -i .../power/zones/5/hourly` → 콘솔 스택트레이스 → 가설 1/2/3 식별
- **1-B 수정** (가설 1 채택 시 예상): `SensorLogRepository.findHourlyPowerProjection` alias 정정 또는 `HourlyPowerProjection` getter 에 `@Value("#{target.device_id}")` 명시 + `PowerServiceIntegrationTest` 신규 추가 (V8 시드 기반)
- **커밋**: `fix(power): GET /power/zones/{id}/hourly 500 오류 해결`

#### 묶음 1 시각 검증 동선

- BE: `./gradlew bootRun --args='--spring.profiles.active=local'`
- FE: `SmartOffice-web` `npm run dev` → `http://localhost:5173`
- 검증 항목:
  - `/dashboard` KPI 카드 4종 (현재 출근 / 오늘 예약 / 활성 장치 / 전체 사용자) 복원
  - `/building` PowerHourlyChart + `/zones` G5 ZonePowerTab 복원
- 통과 시 `PLAN_3_MASTER.md` 9절 #1 + #5 마감 표기 (`feature/backend-fixes` 위 docs 커밋)

### 3-2. 묶음 2 — 스키마 변경 (#8 + #16)

#### #8 access_logs.auth_result V9 마이그레이션 (ALLOW → APPROVED)

- **마이그레이션 파일**: `V9__migrate_allow_to_approved.sql`
  ```sql
  UPDATE access_logs SET auth_result = 'APPROVED' WHERE auth_result = 'ALLOW';
  ```
- **검증**: 마이그레이션 후 `SELECT DISTINCT auth_result FROM access_logs` → `APPROVED / DENIED / BLOCKED` 만 반환
- **커밋**: `chore(migration): V9 access_logs ALLOW → APPROVED 통일`

#### #16 parking_spots 좌표 UNIQUE + null XOR 가드 (V10)

- **마이그레이션 파일**: `V10__parking_spots_unique_position.sql`
  ```sql
  ALTER TABLE parking_spots
    ADD CONSTRAINT UQ_PARKING_SPOTS_POSITION UNIQUE (zone_id, position_x, position_y);
  ```
- **Service 충돌 검증** (`ParkingServiceImpl`):
  - `createSpot`: 좌표 둘 다 not null 시 `existsByZone_ZoneIdAndPositionXAndPositionY(zoneId, x, y)` → 충돌 시 `ErrorCode.DUPLICATE_SPOT_POSITION`
  - `updateSpot`: 자기 자신 제외 검증
- **null XOR 가드**: `ParkingSpotCreateRequest` / `UpdateRequest` 에 bean validation custom annotation 또는 service 진입 직후 `(positionX == null) != (positionY == null)` 차단 → `ErrorCode.INVALID_POSITION_PAIR`
- **커밋**: `feat(parking): #16 좌표 UNIQUE 제약 + null XOR 가드 (V10)`

#### 묶음 2 시각 검증 동선

- `/access-logs` 페이지에서 `authResult` 필터 `APPROVED` 표기 일관성 확인 (V8 시드 기반 ALLOW 잔존 해소)
- `/parking-spots` 등록/수정 핸들러에서 같은 zone 동일 좌표 spot 등록 시도 → 백엔드 4xx + 토스트 노출

### 3-3. 묶음 3 — 신설 엔드포인트 (#9 + #10 + #12 + #15)

#### #9 GET /api/v1/power/zones

- POWER 미터 보유 zone 집계 (`sensor_logs.sensor_type='POWER'` distinct)
- 응답: `[{ "zoneId", "zoneName", "meterCount" }]`
- 권한: `@PreAuthorize("hasRole('ADMIN')")`
- **커밋**: `feat(power): #9 GET /power/zones 신설`

#### #10 GET /api/v1/zones/{id}

- `ZoneController` 에 GET /{id} 추가
- `ZoneListItemResponse` 재사용 또는 `ZoneDetailResponse` 신설 (`childCount` · `deviceCount` · `activeReservationCount` 등 detail 전용 집계)
- `ZoneService.getZoneDetail(Long id)` 도입, 부재 시 `CustomException(ZONE_NOT_FOUND)`
- **커밋**: `feat(zone): #10 GET /zones/{id} 신설`

#### #12 ControlCommandType enum 정합

- 옵션 (A) 채택: `ControlCommandType` enum 정의 (`AC | LIGHT | FAN | DOOR_LOCK | SET_TEMPERATURE`)
- `ControlCommand.commandType` 컬럼 `@Enumerated(EnumType.STRING)` (또는 string 유지하되 service 진입 시 enum 변환 + `INVALID_COMMAND_TYPE`)
- `ControlRequest.command` enum 타입 또는 service 검증
- **커밋**: `feat(control): #12 ControlCommandType enum 정합`

#### #15 GET /api/v1/parking/zones

- 주차면 1건 이상 보유 zone 집계
- 응답: `[{ "zoneId", "zoneName", "zoneType", "totalSpots", "occupiedSpots" }]`
- 권한: ADMIN
- **커밋**: `feat(parking): #15 GET /parking/zones 신설`

#### 묶음 3 시각 검증 동선

- `/zones` 상세 화면 (G5) 에서 백엔드 GET /zones/{id} 호출 정합
- `/power` 위젯에서 `usePowerZones()` 동작 확인 (web 측 `POWER_ZONES_TEMP` 우회 후속 정리는 본 sprint 외 web 작업)
- `/control` 명령 발송 시 잘못된 enum 값 거부 확인
- `/parking` 페이지 zone 필터에서 주차면 보유 zone 만 노출 확인

### 3-4. 묶음 4 — #14 모델 신설 (옵션 A)

**사용자 결정 (2026-05-15)**: 옵션 A — Vehicle 엔티티 신설 + ParkingReservation 분리.

단일 PR 정책은 유지하되, 묶음 4 내부를 2개 sub-bundle 로 분리하여 빌드/테스트/시각 검증 단위 명확화.

#### 4-a Vehicle 엔티티

- **테이블**: `vehicle` (Flyway V11)
  - `id` · `plate_number` (UNIQUE) · `owner_name` · `owner_user_id?` (FK users) · `type` enum [`STAFF` | `VISITOR`] · `purpose?` · `created_at`
- **컴포넌트**:
  - Entity `Vehicle`
  - Repository `VehicleRepository`
  - Service `VehicleService` / `VehicleServiceImpl`
  - Controller `VehicleController` (CRUD 5종)
  - DTO `VehicleCreateRequest` / `VehicleUpdateRequest` / `VehicleResponse`
- **단위 테스트**: `VehicleControllerTest` + `VehicleServiceTest` (CRUD + 권한)
- **빌드/테스트 통과 게이트**: `./gradlew build && ./gradlew test`
- **시연 동선** (4-a 단독): VehicleController curl CRUD (web 측 미구현 가능성 큼)
- **커밋**: `feat(vehicle): #14 Vehicle 엔티티 + CRUD (V11)`

#### 4-b ParkingReservation 엔티티

- **테이블**: `parking_reservation` (Flyway V11 추가 — V11 1개 마이그레이션에 vehicle + parking_reservation 두 테이블 함께 정의 또는 V11 vehicle / V12 parking_reservation 로 분리. 단일 V11 권장)
  - `id` · `vehicle_id` (FK vehicle) · `zone_id` (FK zones) · `spot_id?` (FK parking_spots) · `reserved_at` · `entry_at?` · `exit_at?` · `status` enum [`RESERVED` | `PARKED` | `EXITED`]
- **컴포넌트**:
  - Entity `ParkingReservation`
  - Repository `ParkingReservationRepository`
  - Service `ParkingReservationService` / `ParkingReservationServiceImpl`
  - Controller `ParkingReservationController` (CRUD 5종)
  - DTO 풀셋
- **단위 테스트**: `ParkingReservationControllerTest` + `ParkingReservationServiceTest`
- **빌드/테스트 통과 게이트**
- **시연 동선** (4-b 단독): 4-a Vehicle 생성 → ParkingReservation 연결 curl 흐름
- **커밋**: `feat(parking): #14 ParkingReservation 엔티티 + CRUD (V11)`

#### 묶음 4 시각 검증 동선 (4-b 종료 시 통합)

- `/parking` 차량/예약 신규 화면 (web 측 미구현 시 SmartOffice-web/SUGGESTIONS.md append)
- BE 단독 검증: Vehicle + ParkingReservation curl CRUD + 연결 흐름 정상

### 3-5. 묶음 5 — 후순위 (#1 + #2 + #3 + #4 + #5 + #6)

#### 항목별 시연 동선 / 시연 불가 시 처리

| # | 항목 | 시연 동선 | 시연 불가 시 |
|---|------|----------|------------|
| #1 | guest 도메인 | web 미구현 — 시연 불가 | BE 빌드/테스트 + curl 검증만으로 게이트 통과. SmartOffice-web/SUGGESTIONS.md append (web 측 guest UI) |
| #2 | Refresh Token httpOnly 쿠키 | web `/login` → 로그인 → 토큰 갱신 흐름 (web 영향 큼) | web 코드 미수정 정책상 cookie 동작 검증 어려움 — curl `Set-Cookie` 헤더 검증으로 대체 + SmartOffice-web/SUGGESTIONS.md append |
| #3 | user_preferences | web 미구현 — 시연 불가 | BE 빌드/테스트 + curl 검증. SmartOffice-web/SUGGESTIONS.md append |
| #4 | OpenAPI 보강 | `http://localhost:8080/swagger-ui` 진입 + 신규 스키마 확인 | 시연 가능 (web 무관) |
| #5 | errorCode 필드 | curl 4xx 응답 `errorCode` 필드 확인 | 시연 가능 (web 무관) |
| #6 | reservation 권한 분기 | web `/meeting-rooms` ADMIN/USER 권한별 동작 (web 영향 가능) | curl 권한별 응답 검증 + SmartOffice-web/SUGGESTIONS.md append (web UX 영향 시) |

#### #2 처리 방식 (재확인)

- **BE 코드 수정 포함**: `SecurityConfig` (CORS allow-credentials, exposed/allowed headers, SameSite), `AuthController.login` 응답 `Set-Cookie` 발급, `AuthController.refresh` 쿠키 read
- **web 동반 변경은 SmartOffice-web/SUGGESTIONS.md append 만** (web 코드 직접 수정 금지). append 항목:
  - `axios` `withCredentials: true` 활성화 (`src/lib/api/client.ts`)
  - `tokenStorage.refreshToken` 관련 함수 제거
  - 운영 도메인 통일 시 `Domain` 속성 정합

#### 묶음 5 커밋 분리

각 항목별 1 커밋:

- `feat(guest): #1 guest 도메인 신설`
- `feat(auth): #2 Refresh Token httpOnly 쿠키 전환`
- `feat(user): #3 user_preferences API 신설`
- `chore(openapi): #4 응답 schema 보강`
- `feat(error): #5 errorCode 필드 추가`
- `refactor(reservation): #6 권한 분기 일관성 정리`

---

## 4. 묶음 단위 시각 검증 프로토콜

### 묶음 종료 조건

- 묶음 내 모든 BE 항목의 빌드/테스트 통과 (`./gradlew build && ./gradlew test`)
- 회귀 방지: 본 묶음의 controller 테스트 + 기존 통합 테스트 0건 실패
- 커밋 컨벤션: `fix(domain): ...` / `feat(domain): ...` / `chore(...)` (Conventional Commits)
- 커밋 푸터에 `Co-Authored-By: Claude` / `🤖 Generated with` 0건

### 묶음 보고 (사용자 시각 검증 진입 전)

- 변경 파일 목록 + 신규 엔드포인트 + Flyway 마이그레이션 + 회귀 테스트 결과 요약

### 사용자 시각 검증 절차

- BE: `./gradlew bootRun --args='--spring.profiles.active=local'`
- FE: `SmartOffice-web` 에서 `npm run dev` → `http://localhost:5173`
- 묶음별 시연 동선은 3절 묶음 상세의 "시각 검증 동선" 참조

### 시연 불가 게이트 정책

시연 동선이 web 측 미구현으로 검증 불가한 경우, BE 빌드/테스트 + curl 검증 통과를 게이트 통과로 인정. web 측 영향 항목은 `SmartOffice-web/SUGGESTIONS.md` append 의무.

### 검증 결과 처리

- **통과**: 6절 트래커에 "검증 통과 (YYYY-MM-DD)" 표기 후 다음 묶음 진입
- **결함 발견**:
  - BE 결함: 묶음 내 추가 BE 수정 → 재검증
  - web 결함: `SmartOffice-web/SUGGESTIONS.md` append 후 다음 묶음 진입 (web 코드 미수정)

---

## 5. 머지 정책 (단일 PR)

- 본 sprint 잔여 15건 전체를 **단일 PR 1개** 로 머지
- 단일 브랜치 `feature/backend-fixes` 위에 항목별 커밋 분리 (트레이서빌리티 보존)
- **push 마지막 1회**: 묶음 1~5 모두 종료 + 모든 시각 검증 통과 후 단일 push
- branch protection rule (server origin/main PR 필수) 정합 — 단일 PR 통과로 마감
- PR 본문에 본 마스터플랜 6절 트래커 + `SmartOffice-web/SUGGESTIONS.md` append 항목 정리 포함

### web/SUGGESTIONS.md append 항목 정리 (sprint 종료 시점 PR 본문에 포함)

본 sprint 진행 중 발견되어 SmartOffice-web/SUGGESTIONS.md 에 append 한 항목을 PR 본문에 표 형식으로 정리한다 (web 후속 작업 입력으로 활용). 예상 카테고리:

- 묶음 3 종료 후: `POWER_ZONES_TEMP` 제거 권장 (#9 채택), `useZoneDetail` find 우회 제거 권장 (#10 채택), `useParkingSpots({})` distinct 우회 제거 권장 (#15 채택)
- 묶음 2 종료 후: `accesslog/types.ts` `"ALLOW"` 호환 제거 권장 (#8 V9 후), `ParkingSpotsTable` 사전 검증 제거 권장 (#16 V10 후)
- 묶음 4 종료 후: ParkingManagement 차량/예약 UI 신설 권장 (#14 채택)
- 묶음 5 종료 후: #2 / #6 web 영향 항목

---

## 6. 진행 트래커

| 묶음 | 항목 | 상태 | 커밋 SHA | 시작 | 완료 | 시각 검증 | 비고 |
|------|------|------|----------|------|------|-----------|------|
| 마스터플랜 | 단일 sprint 정책 전환 | 완료 | (본 docs 커밋) | 2026-05-15 | 2026-05-15 | — | 본 문서 |
| — | #13 zone PUT deserialize | 완료 | `a32d146` (PR #27) | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | sprint 단일화 전 별도 PR 머지 완료. 옵션 A + Zone.update() partial update null 가드 동봉 |
| 1 | #7 dashboard summary 500 | 완료 | `d96e77d` (fix) + `5edb416` (test) | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | `DeviceRepository.countByDeviceStatus` 시그니처 String → DeviceStatus enum (Hibernate 7.2.7 type mismatch 가 진짜 원인). 데이터 없음 케이스 단위 테스트 추가 |
| 1 | #11 power hourly 500 | 완료 | `bfe28be` (fix) + `4a8d744` (test) | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | `findHourlyPowerProjection` GROUP BY DATE_FORMAT 패턴 SELECT 와 통일 (only_full_group_by 가 진짜 원인, 마스터플랜 가설 1 projection 매핑은 무관). PowerServiceIntegrationTest 신설 |
| 1 | 묶음 1 시각 검증 | 완료 | — | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | /dashboard KPI 4종 (현재 출근 0/10·오늘 예약 5·활성 장치 18·전체 사용자 10) + /building 시간별 전력 + /zones 전력 탭 복원 |
| 2 | #8 V9 ALLOW→APPROVED | 완료 | `55a6c37` | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | curl: ALLOW 0건 / APPROVED 61건 (이전 53+8). web 측 ALLOW literal 호환 제거 권장 SUGGESTIONS append |
| 2 | #16 parking_spots V10 UNIQUE + null XOR | 완료 | `7975c49` (fix) + `3101713` (test) | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | UNIQUE 제약 + Service validatePosition (null XOR + 좌표 충돌) + ErrorCode 2건. curl: 동일 좌표 409 + null XOR 400 |
| 2 | 묶음 2 시각 검증 | 완료 | — | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | BE curl 검증 통과 (#8 ALLOW→APPROVED 정합 + #16 좌표 충돌 차단 정합). web 측 결함 발견(Select 영어 표시값 + ALLOW 옵션 잔존) → web/SUGGESTIONS.md #2 append |
| 3 | #9 GET /power/zones | 완료 | `6445b64` | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | POWER 미터 보유 zone 집계 + meterCount. curl zone 4건 |
| 3 | #10 GET /zones/{id} | 완료 | `154636a` | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | ZoneListItemResponse 재사용. curl 6 필드 정합 |
| 3 | #12 ControlCommandType enum | 완료 | `4af6d36` (fix) + `ec58654` (test) | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | enum 5종 + INVALID_COMMAND_TYPE. curl POWER_ON 400 거부 |
| 3 | #15 GET /parking/zones | 완료 | `513732c` | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | 주차면 보유 zone + totalSpots/occupiedSpots. curl zone 2건 |
| 3 | 묶음 3 시각 검증 | 완료 | — | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | BE curl 4건 통과 + web 5개 페이지 회귀 0건. 신설 EP web 채택은 후속 (web/SUGGESTIONS.md #3) |
| 4 | 4-a Vehicle 엔티티 (V11) | 완료 | `848ff31` | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | 옵션 A. Vehicle 엔티티 + CRUD 5 + VehicleType enum. 단위/통합 테스트 8 |
| 4 | 4-b ParkingReservation 엔티티 (V12) | 완료 | `b7d9809` | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | ParkingReservation 엔티티 + CRUD 5 + 상태 RESERVED/PARKED/EXITED. 단위/통합 테스트 7. V11(vehicle)/V12(parking_reservation) 분리 |
| 4 | 묶음 4 시각 검증 | 완료 | — | 2026-05-15 | 2026-05-15 | 통과 (2026-05-15) | 시연 불가 게이트 — BE curl 통합 흐름 (Vehicle 생성 → Reservation 연결 → 입차 PARKED → DELETE 정리) 통과. web 미구현 → SUGGESTIONS #4 append |
| 5 | #1 guest 도메인 | 대기 | — | — | — | — | web 미구현 — curl 검증 + SUGGESTIONS append |
| 5 | #2 Refresh Token httpOnly | 대기 | — | — | — | — | BE 수정 + web SUGGESTIONS append |
| 5 | #3 user_preferences | 대기 | — | — | — | — | web 미구현 — curl 검증 + SUGGESTIONS append |
| 5 | #4 OpenAPI 보강 | 대기 | — | — | — | — | 시연 가능 |
| 5 | #5 errorCode 필드 | 대기 | — | — | — | — | 시연 가능 |
| 5 | #6 reservation 권한 분기 | 대기 | — | — | — | — | 시연 가능 + SUGGESTIONS append |
| 5 | 묶음 5 시각 검증 | 대기 | — | — | — | — | sprint 종료 |
| 마지막 | 단일 PR open + push | 대기 | — | — | — | — | 모든 묶음 종료 후 1회 push |

---

## 7. 참고 경로

- `BACKEND_SUGGESTIONS.md` — 누적 제안 16건 (#1~#16, 영구 보존)
- `../SmartOffice-web/docs/PLAN_3_MASTER.md` — web 통합 작업 마스터플랜 + 9절 잔존 결함 추적표
- `../SmartOffice-web/SUGGESTIONS.md` — web 측 누적 제안 (본 sprint 진행 중 append 추가)
- `CLAUDE.md` · `AGENTS.md` · `GEMINI.md` — 백엔드 컨벤션 (3종 동일 동기화)
- `docs/erd.sql` · `docs/requirements.md` · `docs/mqtt-prod-deploy.md` — 도메인 스펙
