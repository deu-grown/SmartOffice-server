# 백엔드 수정 마스터플랜

> 작성일: 2026-05-15
> 상태: 플랜 1 진입 대기 (0단계 read-only 검증 완료)
> 입력: `BACKEND_SUGGESTIONS.md` #1~#16 + `../SmartOffice-web/docs/PLAN_3_MASTER.md` 9절 잔존 결함 추적표 #1~#10
> 작업 브랜치: `feature/backend-fixes` (server 레포, main 기반)
> 머지 정책: branch protection rule 준수 → 처음부터 PR 절차 (각 항목 독립 PR)
> 다음 단계: 별도 plan mode 세션에서 플랜 1 #7 1-A 진단 진입

본 문서는 SmartOffice-server 백엔드 수정 작업의 단일 진실 공급원이다. `BACKEND_SUGGESTIONS.md` 와의 책임 분담은 다음과 같다.

- **`BACKEND_SUGGESTIONS.md`** = 프론트 통합 작업에서 누적된 제안의 영구 보존 (출처 세션 · 우선순위 · 근거 기록). 처리 완료 시 본문에 "처리 완료" 표기 추가만 수행.
- **`BACKEND_FIX_MASTER.md`** (본 문서) = 실제 실행 계획. 플랜 분할 · 작업 순서 · PR 단위 · 진행 트래커 보유.

---

## 0. 컨텍스트

`SmartOffice-web` 통합 작업(2026-05-14~15) 종료 후, web 측은 모든 결함에 graceful handling(우회 상수 · `isError` + ErrorBoundary · 사전 검증 가드) 적용 완료하여 시연 동작은 가능하나 다음 3 기능이 영구 차단 상태:

| 기능 | 차단 원인 | 결함 ID |
|------|----------|---------|
| /dashboard KPI 카드 4종 (현재 출근/오늘 예약/활성 장치/전체 사용자) | `/dashboard/summary` 500 | #7 |
| /building PowerHourlyChart + /zones G5 ZonePowerTab | `/power/zones/{id}/hourly` 500 | #11 |
| /zones G5 ZoneInfoTab 수정 모달 | `PUT /zones/{id}` body deserialize 실패 | #13 |

본 플랜 1 의 목표 = 위 3건의 백엔드 수정. web 측은 검증(curl + 브라우저)만, 코드 수정 권한 없음.

---

## 1. 우선순위 그룹

- **상**: #7 · #11 · #13 (3건) — 핵심 기능 차단, 시연 영향
- **중**: #8 · #9 · #10 · #12 · #15 · #16 (6건) — 기능 보조 또는 안전망
- **저~중**: #14 (1건) — PM 결정 사안 (Vehicle/Reservation 모델 신설)
- **후순위(저)**: #1 · #2 · #3 · #4 · #5 · #6 (6건) — web 통합 전 누적된 제안

---

## 2. 플랜 분할

| 플랜 | 그룹 | 항목 | PR 개수 |
|------|------|------|---------|
| **1** | 우선순위 상 (단일 플랜, 각각 별도 PR) | #7 dashboard summary 500 / #11 power hourly 500 / #13 zone PUT deserialize | **3** |
| **2** | 누락 엔드포인트 신설 | #9 GET /power/zones / #10 GET /zones/{id} / #12 control_commands enum 또는 메타 / #15 GET /parking/zones | TBD (2~4) |
| **3** | 데이터 정합 + 제약 | #8 access_logs ALLOW 마이그레이션 / #16 parking_spots 좌표 UNIQUE + null XOR | TBD (1~2) |
| **4** | 모델 신설 검토 (PM 결정 사안) | #14 Vehicle/ParkingReservation | TBD |

플랜 2~4 의 상세는 플랜 1 완료 후 별도 plan mode 세션에서 정의한다.

---

## 3. 플랜 1 상세 — 우선순위 상 3건

### 3-1. 0단계 read-only 검증 결과 (2026-05-15)

#### #7 GET /api/v1/dashboard/summary HTTP 500

`DashboardService.java:45-58` 확인:

- 4 카운트 모두 `(long → int)` 캐스팅 — `userRepository.countByStatus(UserStatus.ACTIVE)` · `deviceRepository.countByDeviceStatus("ACTIVE")` · `reservationRepository.countTodayConfirmed(ReservationStatus.CONFIRMED, startOfDay, endOfDay)` · `pendingApprovals=0` 하드코딩
- NPE 경로 없음. 진짜 원인 후보:
  - (a) `countByDeviceStatus(String)` — `DeviceRepository.java:19` 시그니처는 String. `Device.deviceStatus` 컬럼 타입이 enum 이면 단순 0 반환이라 무해. **확인 필요**.
  - (b) `countTodayConfirmed(ReservationStatus, LocalDateTime, LocalDateTime)` JPQL 쿼리 결함 가능.
  - (c) Spring Boot 4 / Hibernate 6.x 의 다른 쿼리 결함.
- **사용자 결정 2026-05-15**: 본문 추정(NPE) 유지 + 1-A(진단) → 1-B(수정) 2단계로 분리. 실제 진단은 별도 세션.

#### #11 GET /api/v1/power/zones/{zoneId}/hourly HTTP 500

`PowerService.java:44-75` + `SensorLogRepository.java:55-76` + `HourlyPowerProjection.java` 확인:

- Native query alias snake_case: `id` · `device_id` · `device_name` · `hour_at` · `kwh` · `avg_watt` · `peak_watt`
- Projection getter camelCase: `getId()` · `getDeviceId()` · `getDeviceName()` · `getHourAt()` · `getKwh()` · `getAvgWatt()` · `getPeakWatt()`
- **가설 1 (유력)**: Spring Boot 4 / Hibernate 6.x 의 nativeQuery + interface projection 매핑에서 snake_case alias → camelCase getter 자동 변환 미작동. `@Value("#{target.device_id}")` 명시 또는 alias 를 camelCase 로 변경 필요.
- 가설 2: `LocalDateTime.parse(p.getHourAt(), fmt)` — `DATE_FORMAT` 결과가 `'%Y-%m-%dT%H:00:00'` 패턴 → 서비스 `"yyyy-MM-dd'T'HH:mm:ss"` 와 일치. 가설 1 통과 시 무해.
- `/billing` 엔드포인트는 entity 기반 응답이라 정상 작동 — 정합.
- **사용자 결정 2026-05-15**: #7 과 동일하게 1-A(진단) → 1-B(수정) 2단계 분리.

#### #13 PUT /api/v1/zones/{id} body deserialize 결함

`ZoneUpdateRequest.java` + `ZoneCreateRequest.java` 확인:

- 둘 다 `@Getter` + `@NoArgsConstructor`. Setter 부재. `@JsonCreator`/`@JsonProperty` 부재.
- **결정적 차이**: `ZoneUpdateRequest` 에만 `private boolean clearParent` (primitive). `ZoneCreateRequest` 는 primitive boolean 필드 없음.
- Lombok `@Getter` 가 `isClearParent()` 생성 → Jackson property "clearParent" 추론. setter 부재 시 read-only property 로 간주되어 unknown property 또는 mapping 예외 가능.
- **사용자 결정 2026-05-15**: 옵션 (A) 채택 — `Boolean` wrapper + `@Setter` 추가. 본 세션 가설 정확 일치로 1-A 진단 단계 불필요, 즉시 수정 가능.

### 3-2. #7 dashboard/summary 500 — 1-A 진단 → 1-B 수정 (2단계)

**1-A 진단** (별도 plan mode 세션에서 진행):

1. `./gradlew bootRun --args='--spring.profiles.active=local'` 기동 (로컬, 8080)
2. admin@grown.com 토큰 발급 → `curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/dashboard/summary`
3. 콘솔 스택트레이스 1차 확인. 결함 메서드 식별:
   - `userRepository.countByStatus(UserStatus.ACTIVE)` 단독 호출 검증
   - `deviceRepository.countByDeviceStatus("ACTIVE")` 단독 호출 검증 (Device.deviceStatus 컬럼 타입 enum vs String 확인)
   - `reservationRepository.countTodayConfirmed(...)` JPQL 검증
4. 결함 메서드 확정 후 본 문서의 1-B 수정 항목 구체화

**1-B 수정** (1-A 결과 기반):

- 결함 메서드의 NPE/JPQL/시그니처 결함 fix
- `DashboardService.getSummary` 의 4 필드 정규화 (모두 정수 0 이상 보장)
- `DashboardServiceTest` 데이터 없음 케이스 단위 테스트 추가

**파일 (1-B 단계 예상)**:

- `domain/dashboard/service/DashboardService.java` (수정)
- `domain/dashboard/service/DashboardServiceTest.java` (신규 또는 보강)
- 결함 메서드의 repository 또는 JPQL 정정 (1-A 결과 따라 결정)

**PR**: `fix(dashboard): GET /dashboard/summary 500 오류 해결` — 1-A 진단 결과를 PR 본문 첫 줄에 명시

### 3-3. #11 power hourly 500 — 1-A 진단 → 1-B 수정 (2단계)

**1-A 진단** (별도 plan mode 세션에서 진행):

1. `./gradlew bootRun --args='--spring.profiles.active=local'` 기동
2. admin@grown.com 토큰 발급 → `curl -i -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/power/zones/5/hourly"` (V8 시드 POWER zone 4건 중 zone 5)
3. 콘솔 스택트레이스 1차 확인. 결함 식별:
   - 가설 1 검증: nativeQuery + interface projection 의 snake_case alias 매핑 실패 (`IllegalArgumentException` 또는 `MappingException` 예상)
   - 가설 2 검증: `LocalDateTime.parse(p.getHourAt(), fmt)` 의 `DateTimeParseException` (가설 1 통과 시)
   - 가설 3 검증: native query 자체 실패 (SQL syntax / column not found 등)
4. 결함 위치 확정 후 본 문서의 1-B 수정 항목 구체화

**1-B 수정** (1-A 결과 기반):

가설 1 채택 시 예상 수정:

- `SensorLogRepository.findHourlyPowerProjection` 의 alias 를 camelCase 로 정정 (`devices_id AS deviceId` 등)
- 또는 `HourlyPowerProjection` getter 에 `@Value("#{target.device_id}")` 명시 (정합 유지 옵션)
- 통합 테스트 `PowerServiceIntegrationTest.getHourlyHistory_returnsResponse_whenPowerLogsExist` 신규 추가 (V8 시드 기반 POWER zone 4건)

**파일 (1-B 단계 예상, 가설 1 채택 시)**:

- `domain/sensor/repository/SensorLogRepository.java` (수정)
- `domain/power/service/PowerServiceTest.java` 또는 `PowerControllerTest.java` (신규/보강)

**PR**: `fix(power): GET /power/zones/{id}/hourly 500 오류 해결` — 1-A 진단 결과를 PR 본문 첫 줄에 명시

### 3-4. #13 zone PUT body deserialize 결함 (1단계, 즉시 수정 가능)

**근거**: 0단계 가설 정확 일치. `ZoneUpdateRequest.clearParent` primitive boolean + setter 부재.

**옵션 (A) 채택** (사용자 결정 2026-05-15):

```java
@Getter
@Setter
@NoArgsConstructor
public class ZoneUpdateRequest {
    ...
    private Boolean clearParent;  // primitive → wrapper
}
```

+ `ZoneServiceImpl.updateZone` 내부 `request.getClearParent()` 사용 시 `Boolean.TRUE.equals(...)` null safe 처리

**대안 (B) 미채택**: `@Builder` + `@AllArgsConstructor` + `@Jacksonized` 로 record-like 불변 DTO 재설계 — 변경 폭이 커서 회귀 위험 ↑

**파일**:

- `domain/zone/dto/ZoneUpdateRequest.java` (수정 — `@Setter` 추가 + `clearParent` Boolean wrapper)
- `domain/zone/service/ZoneServiceImpl.java` (수정 — `getClearParent()` null safe 처리)
- `domain/zone/controller/ZoneControllerTest.java` (신규 또는 보강) — PUT body 4 변형:
  1. `{"name": "x"}` (name 만)
  2. `{"parentId": 1}` (parentId 만)
  3. `{"clearParent": true}` (clearParent true)
  4. `{"name":"x","zoneType":"ROOM","parentId":null,"clearParent":true,"description":"y"}` (전체)
  - 모두 200 응답 + DB 반영 확인

**참고**: `ZoneCreateRequest` 도 setter 부재라 동일하게 fields-based deserialize 에 의존하지만, primitive boolean 필드가 없어 통과 중. 본 수정에서 함께 `@Setter` 추가는 권장 X (POST 정상 동작 영향 가능). ZoneUpdateRequest 만 수정 범위.

**PR**: `fix(zone): PUT /zones/{id} body deserialize 결함 해결 (clearParent Boolean wrapper)`

### 3-5. 게이트 (각 PR 머지 직전)

- `./gradlew build` 통과
- `./gradlew test` 통과 (신규 테스트 포함)
- 회귀 방지: 본 PR 의 controller 테스트 + 기존 통합 테스트 0건 실패
- 커밋 메시지에 `Co-Authored-By: Claude` / `🤖 Generated with` 푸터 0건 (전역 규칙 정합)
- 커밋 컨벤션: `fix(domain): ...` (Conventional Commits)

### 3-6. web 잔존 게이트 마감 (백엔드 수정 후 검증)

본 플랜 1 PR 3건 머지 후, web 측 재호출 검증을 별도 세션에서 수행 (단, web 수정 권한 없음 — 검증만):

| # | 백엔드 수정 | web 재검증 | 추적표 마감 |
|---|------------|-----------|------------|
| 1 | #7 dashboard summary 500 → 200 | /dashboard KPI 4종 복원 (curl + 브라우저) | `PLAN_3_MASTER.md` 9절 #1 "마감 (백엔드 fix `<SHA>`)" |
| 5 | #11 power hourly 500 → 200 | /building PowerHourlyChart 복원 + /zones G5 ZonePowerTab 복원 | 9절 #5 "마감 (백엔드 fix `<SHA>`)" |
| 7 | #13 zone PUT 500 → 200 | /zones G5 ZoneInfoTab 수정 모달 저장 성공 | 9절 #7 "마감 (백엔드 fix `<SHA>`, Fix 7 부분)" |

**web SUGGESTIONS append (수정 권한 외 — 안내만)**:

- web `SUGGESTIONS.md` 신설 또는 BACKEND_SUGGESTIONS 패턴 차용 — 신설 시 사용자 명시적 허락 필요
- 백엔드 sprint 종료 시점에 안내 항목 작성:
  - `features/power/constants.ts` 의 `POWER_ZONES_TEMP` 제거 권장 (백엔드 #9 채택 시)
  - `features/zone/hooks.ts:useZoneDetail` 의 `find(id)` 우회 제거 권장 (백엔드 #10 채택 시)
  - `features/accesslog/types.ts` 의 `"ALLOW"` 호환 제거 권장 (백엔드 #8 V9 마이그레이션 후)
  - `ParkingManagement` 의 `useParkingSpots({})` distinct 우회 제거 권장 (백엔드 #15 채택 시)
  - `ParkingSpotsTable` 의 `validateCoordinates()` 사전 검증 제거 권장 (백엔드 #16 채택 시)

---

## 4. 플랜 2~4 골격 (TBD)

### 플랜 2 (누락 엔드포인트 신설)

- **#9 GET /api/v1/power/zones** — `sensor_logs.sensor_type='POWER'` distinct zone 집계. 응답에 `zoneId` · `zoneName` · `meterCount` (또는 향후 `totalSpots` 패턴과 정합 위해 보강).
- **#10 GET /api/v1/zones/{id}** — `ZoneListItemResponse` 재사용 또는 `ZoneDetailResponse` 신설 (`childCount` · `deviceCount` · `activeReservationCount` 등 detail 전용 집계).
- **#12 ControlCommandType enum 또는 GET /api/v1/controls/commands 메타** — (A) enum 정의 + 검증, 또는 (B) 메타 엔드포인트. 또는 (A) + (B) 조합.
- **#15 GET /api/v1/parking/zones** — `parking_spots` 보유 zone + `totalSpots`/`occupiedSpots` 포함.

### 플랜 3 (데이터 정합 + 제약)

- **#8 V9__migrate_allow_to_approved.sql Flyway 마이그레이션** — `UPDATE access_logs SET auth_result = 'APPROVED' WHERE auth_result = 'ALLOW';` 1회 실행. 향후 enum 컬럼화 검토.
- **#16 parking_spots (zone_id, position_x, position_y) UNIQUE + null XOR 가드** — V9 또는 V10 마이그레이션 + `ParkingServiceImpl` Service 충돌 검증 + DTO null XOR validation.

### 플랜 4 (모델 신설 검토, PM 결정 후)

- **#14 Vehicle + ParkingReservation 또는 AccessLog 통합 또는 외부 ALPR** — 옵션 결정 선행. PM 결정 사안.

### 후순위 (필요 시 별도 sprint)

- #1 guest 도메인 / #2 Refresh Token httpOnly 쿠키 / #3 user_preferences / #4 OpenAPI 보강 / #5 errorCode 필드 / #6 reservation 권한 분기

---

## 5. 검증 / 머지 정책

- 각 PR 단독 머지 (squash X — 본 fix 들은 분리 의미)
- branch protection rule 정합: server origin/main 직접 push 금지 → 본 작업 전체 PR 절차
- web 수정 권한 외 — 발견 시 SUGGESTIONS append 사용자 허락 후
- `./gradlew build` + `./gradlew test` 통과 후 PR open

---

## 6. 진행 트래커

| 플랜 | 항목 | 상태 | PR | 시작 | 완료 | 비고 |
|------|------|------|----|----|------|------|
| 마스터플랜 작성 | — | 완료 | — | 2026-05-15 | 2026-05-15 | 본 문서 |
| 1 | #7 dashboard summary 500 (1-A) | 대기 | — | — | — | 별도 plan mode 세션 |
| 1 | #7 (1-B) | 대기 | — | — | — | 1-A 완료 후 |
| 1 | #11 power hourly 500 (1-A) | 대기 | — | — | — | 별도 plan mode 세션 |
| 1 | #11 (1-B) | 대기 | — | — | — | 1-A 완료 후 |
| 1 | #13 zone PUT deserialize | 완료 | #27 (`a32d146`) | 2026-05-15 | 2026-05-15 | 옵션 A 채택 + `Zone.update()` partial update null 가드 동봉. SUGGESTIONS #17 분리 등록. ZoneControllerTest PUT 5 시나리오 + ZoneInfoTab 시각 검증 통과 |
| 2~4 | — | TBD | — | — | — | 플랜 1 완료 후 정의 |

---

## 7. 참고 경로

- `BACKEND_SUGGESTIONS.md` — 누적 제안 16건 (#1~#16, 영구 보존)
- `../SmartOffice-web/docs/PLAN_3_MASTER.md` — web 통합 작업 마스터플랜 + 9절 잔존 결함 추적표
- `CLAUDE.md` · `AGENTS.md` — 백엔드 컨벤션
- `docs/erd.sql` · `docs/requirements.md` · `docs/mqtt-prod-deploy.md` — 도메인 스펙
