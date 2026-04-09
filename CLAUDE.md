# CLAUDE.md — SmartOffice Backend

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
- **DB**: MySQL 8.0 (prod) / H2 (local)
- **Cache**: Redis 7
- **ORM**: Spring Data JPA + MyBatis (복잡한 조회는 MyBatis)
- **인증**: Spring Security + JWT (Access 30분 / Refresh 7일)
- **마이그레이션**: Flyway
- **IoT 통신**: MQTT (Eclipse Paho, QoS Level 1)
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

총 14개 테이블 / 6개 도메인 (상세 스키마는 `@docs/erd.sql` 참조)

| 도메인    | 테이블                                                       |
| --------- | ------------------------------------------------------------ |
| 회원/인증 | `departments`, `users`, `refresh_tokens`                     |
| 공간/장치 | `zones` (self-ref 계층), `devices`                           |
| 출입/근태 | `nfc_cards`, `access_logs`, `attendance`, `monthly_attendance` |
| 급여/ERP  | `salary_settings`, `salary_records`                          |
| 환경/IoT  | `sensor_logs`, `control_commands`                            |
| 예약      | `reservations`                                               |

### 핵심 데이터 흐름

```
NFC 태그
  → access_logs (원시 로그)
    → attendance (당일 배치 집계, 매일 00:05)
      → monthly_attendance (월말 배치 집계)
        → salary_records (급여 자동 산출)
```

---

## 개발 규칙

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

### 네이밍

- Controller: `XxxController`
- Service: `XxxService` / `XxxServiceImpl`
- Repository: `XxxRepository`
- DTO: `XxxRequest` / `XxxResponse`
- Entity: 테이블명 PascalCase (예: `AccessLog`, `SensorLog`)

### 프로필 분리

- `application.yml`: 공통 설정
- `application-local.yml`: 로컬 개발 (H2, 로그 DEBUG)
- `application-prod.yml`: 운영 (MySQL, 로그 INFO)

---

## 비기능 요구사항 (구현 시 준수)

- REST API 응답 시간: **95% 이상 500ms 이내**
- MQTT QoS: **Level 1** (최소 1회 전달 보장)
- 통신: **HTTPS + MQTTS(8883)** (평문 금지)
- 출입 판정 Latency: **1초 이내**

---

## MVP 구현 순서 (스프린트 기준)

1. **Sprint 1**: 인증(JWT), 계정 관리, 부서/직원 CRUD, NFC 카드 등록
2. **Sprint 2~4**: 출입 판정(MQTT), 근태 집계 배치
3. **Sprint 3**: 급여 설정, 월별 집계, 급여 자동 산출
4. **Sprint 5**: 환경 센서 수집/저장, 냉난방 자동 제어
5. **Sprint 6**: 대시보드/앱 연동 API
6. **Sprint 7**: 성능 최적화, 보안 강화, 백업