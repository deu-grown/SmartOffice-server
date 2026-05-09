# AGENTS.md — SmartOffice Backend

## 프로젝트 개요

- **프로젝트명**: SaaS형 스마트 오피스 통합 관리 플랫폼
- **팀명**: 그로운 | 동의대학교 컴퓨터공학과 캡스톤 디자인
- **수행기간**: 2026.03.02 ~ 2026.06.12
- **담당**: 박성종 (팀장 / 백엔드)

---

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.5
- **Build**: Gradle-Groovy
- **패키지 루트**: `com.grown.smartoffice`
- **DB**: MySQL 8.0 (prod·local 공통) / H2 (테스트 전용 runtimeOnly)
- **Cache**: Redis 7
- **ORM**: Spring Data JPA (JpaRepository + @Query JPQL/nativeQuery)
- **인증**: Spring Security + JWT (Access 30분 / Refresh 7일)
- **마이그레이션**: Flyway (V1 스키마 / V2 초기 데이터 / V3 개발 샘플 데이터 / V4 주차·자산·전력 테이블 / V5 통합 테스트 더미데이터 / V6 NFC 카드 상태 컬럼 추가)
- **IoT 통신**: MQTT (Eclipse Paho 1.2.5, QoS Level 1)
- **API 문서**: Springdoc OpenAPI 3.0.2 (Swagger UI: `/swagger-ui.html`)
- **인프라**: AWS EC2 t3.small, Docker, GitHub Actions CI/CD

---

## 문서 참조

- ERD (전체 스키마): `@docs/erd.sql`
- 요구사항 명세: `@docs/requirements.md`
- API 명세: `@docs/api-spec/` (도메인별 md 파일)

---

## 아키텍처

```
RPi5 (Python + SQLite 캐싱)
  → MQTT (Mosquitto, QoS 1, SSL/TLS)
    → Spring Boot API (AWS EC2)
      → MySQL (영구 저장)
      → Redis (Refresh Token, 캐시)
        → React Web / Flutter App
```

### MQTT 토픽 구조

```
smartoffice/{zone_id}/{sensor_type}     # 센서 데이터
smartoffice/{zone_id}/access            # 출입 이벤트
smartoffice/{zone_id}/command           # 제어 명령 (서버 → RPi)
```

---

## ERD 핵심 관계 요약

총 17개 테이블 / 9개 도메인 (상세 스키마는 `@docs/erd.sql` 참조)

| 도메인    | 테이블 | Flyway |
| --------- | ------ | ------ |
| 회원/인증 | `departments`, `users`, `refresh_tokens` | V1 |
| 공간/장치 | `zones` (self-ref 계층), `devices` | V1 |
| 출입/근태 | `nfc_cards`, `access_logs`, `attendance`, `monthly_attendance` | V1 |
| 급여/ERP  | `salary_settings`, `salary_records` | V1 |
| 환경/IoT  | `sensor_logs`, `control_commands` | V1 |
| 예약      | `reservations` | V1 |
| 주차      | `parking_spots` | V4 |
| 자산 관리 | `assets` | V4 |
| 전력 관리 | `power_billing` | V4 |

### 핵심 데이터 흐름

```
NFC 태그
  → access_logs (원시 로그)
    → attendance (일별 배치 집계, 매일 00:05 — AttendanceScheduler)
      → monthly_attendance (월별 배치 집계, 매월 1일 00:10)
        → salary_records (급여 자동 산출)
```

### 배치 스케줄 (AttendanceScheduler)

| 스케줄 | 작업 | 수동 트리거 |
|--------|------|-------------|
| 매일 00:05 | 전일 근태 집계 (`AttendanceBatchService.runDailyBatch`) | `POST /api/v1/attendance/batch` (ADMIN) |
| 매월 1일 00:10 | 전월 월별 집계 (`AttendanceBatchService.runMonthlyAggregation`) | 동일 엔드포인트 |

---

## 개발 규칙

### API 경로 패턴

모든 REST API: `/api/v1/{resource}` (예: `/api/v1/users`, `/api/v1/attendance`)

### 공통 응답 형식

모든 API는 아래 래퍼를 사용한다.

```java
ApiResponse<T> {
    String code;    // "success" | "error"
    String message;
    T data;
}
```

### 예외 처리

- `GlobalExceptionHandler`에서 통일 처리
- 비즈니스 예외는 `CustomException` + `ErrorCode` enum 사용
- 예외 직접 throw 금지, ErrorCode로만 관리

### 트랜잭션

- 서비스 레이어(`@Service`)에서만 `@Transactional` 선언
- 조회 전용 메서드는 `@Transactional(readOnly = true)`

### 보안

- 환경변수 하드코딩 절대 금지 (`.env` / `application-prod.yml` 사용)
- ADMIN 전용 엔드포인트는 `@PreAuthorize("hasRole('ADMIN')")` 필수
- 직원 본인 데이터 접근 시 JWT subject와 PathVariable 대조 검증
- IoT 장치용 엔드포인트(`/api/v1/access-logs/tag`)는 인증 없이 허용 (SecurityConfig permitAll)

### 네이밍

- Controller: `XxxController`
- Service: `XxxService` / `XxxServiceImpl`
- Repository: `XxxRepository`
- DTO: `XxxRequest` / `XxxResponse`
- Entity: 테이블명 PascalCase (예: `AccessLog`, `SensorLog`)

### 프로필 분리

- `application.yml`: 공통 설정
- `application-local.yml`: 로컬 개발 (MySQL localhost:3306, 로그 DEBUG)
- `application-prod.yml`: 운영 (MySQL, 로그 INFO)

---

## 비기능 요구사항 (구현 시 준수)

- REST API 응답 시간: **95% 이상 500ms 이내**
- MQTT QoS: **Level 1** (최소 1회 전달 보장)
- 통신: **HTTPS + MQTTS(8883)** (평문 금지)
- 출입 판정 Latency: **1초 이내**

---

## 현재 구현 상태 (2026-05-09 기준)

### 완전 구현된 도메인

| 도메인 | 주요 구성요소 |
|--------|--------------|
| `auth` | 로그인, 로그아웃, 토큰 갱신, CustomUserDetailsService, 테스트 로그인(`TestAuthController`, `!prod` 전용) |
| `user` | 직원 CRUD, 페이지네이션·필터, 퇴사 처리 |
| `department` | 부서 CRUD |
| `attendance` | 태그 기록, 조회, 수동 보정, 배치 집계, 스케줄러 |
| `salary` | 급여 설정 CRUD, 월별 산출, 확정, 조회 |
| `accesslog` | NFC 태그 처리, MQTT 수신(`AccessLogMqttListener`) |
| `zone` | 구역 CRUD, 계층 트리 조회 |
| `dashboard` | 전체 요약·센서 현황·근태 현황·출입 기록 API (4개) |
| `asset` | 자산 CRUD 5개 API (자산 관리 대장) |
| `device` | 장치 CRUD 5개 API, MQTT 토픽 자동 생성 |
| `nfccard` | NFC 카드 CRUD, 사용자 카드 조회·발급·회수·만료 처리 |
| `sensor` | 센서 로그 수집(`SensorMqttListener`), 구역별 최신·이력 조회 |
| `control` | 냉난방·조명 제어 명령 발송(MQTT), 이력 조회 |

### 미구현 (API 스펙 존재, 코드 없음)

| 도메인 | ERD 테이블 | API 스펙 |
|--------|-----------|---------|
| 예약 관리 | `reservations` (있음) | 8개 API (생성·조회·수정·취소·NFC 체크인 등) |
| 전력 관리 | `power_billing` (V4) | 5개 API (실시간·이력·월요금 산출 등) |
| 주차 관리 | `parking_spots` (V4) | 7개 API (주차면 CRUD·현황·지도·IoT 상태 업데이트) |

---

## MVP 구현 순서 (스프린트 기준)

1. **Sprint 1** ✅: 인증(JWT), 계정 관리, 부서/직원 CRUD
2. **Sprint 2~4** ✅: 출입 판정(MQTT), 근태 집계 배치, 급여 설정·산출
3. **Sprint 5** ✅: NFC 카드 API, 장치 API, 환경 센서 수집/저장, 냉난방 자동 제어
4. **Sprint 6** 🔲: 예약 관리, 전력 관리 *(자산 관리 ✅ 완료)*
5. **Sprint 7** 🔲: 앱 연동 *(대시보드 API ✅ 완료)*
6. **Sprint 8** 🔲: 성능 최적화, 보안 강화, 백업
