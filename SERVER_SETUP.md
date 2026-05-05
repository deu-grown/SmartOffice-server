# SmartOffice 백엔드 서버 로컬 실행 가이드

## 목차

1. [필수 설치 항목](#1-필수-설치-항목)
2. [저장소 클론](#2-저장소-클론)
3. [환경 설정 파일 세팅](#3-환경-설정-파일-세팅)
4. [인프라 서비스 실행 (Docker)](#4-인프라-서비스-실행-docker)
5. [애플리케이션 실행](#5-애플리케이션-실행)
6. [Swagger UI 접속](#6-swagger-ui-접속)
7. [프론트엔드·앱에서 로컬 백엔드 연결](#7-프론트엔드앱에서-로컬-백엔드-연결)
8. [테스트 로그인 엔드포인트](#8-테스트-로그인-엔드포인트)
9. [자주 발생하는 오류 및 해결법](#9-자주-발생하는-오류-및-해결법)

---

## 1. 필수 설치 항목

| 도구 | 버전 | 용도 | 설치 링크 |
|------|------|------|-----------|
| JDK | 21 이상 | 애플리케이션 실행 | [Eclipse Temurin](https://adoptium.net/) |
| Docker Desktop | 최신 | MySQL·Redis·Mosquitto 컨테이너 실행 | [docker.com](https://www.docker.com/products/docker-desktop/) |
| Git | 최신 | 소스 클론 | [git-scm.com](https://git-scm.com/) |

> Gradle은 프로젝트에 포함된 **Gradle Wrapper(`./gradlew`)** 를 사용하므로 별도 설치 불필요.

---

## 2. 저장소 클론

```bash
git clone https://github.com/deu-grown/SmartOffice-server.git
cd SmartOffice-server
```

---

## 3. 환경 설정 파일 세팅

`application-local.yml`은 시크릿 키를 포함하므로 `.gitignore`에 등록되어 있습니다.
아래 순서로 직접 생성해야 합니다.

### 3-1. 템플릿 복사

```bash
cp src/main/resources/application-local.yml.example \
   src/main/resources/application-local.yml
```

### 3-2. 값 채우기

`application-local.yml`을 열고 `<...>` 플레이스홀더를 실제 값으로 교체합니다.

| 항목 | 설명 | 예시 |
|------|------|------|
| `jwt.secret` | HS256 서명용 시크릿 — 32자 이상 임의 문자열 | `MyLocalJwtSecret1234567890ABCDEF!!` |
| `spring.datasource.password` | 로컬 MySQL root 비밀번호 | `1234` (docker-compose 기본값) |

> `spring.datasource.password`는 4단계에서 실행할 `docker-compose.yml`의 `MYSQL_ROOT_PASSWORD`와 동일해야 합니다.
> 기본값(`1234`)을 그대로 사용하면 별도 수정 없이 동작합니다.

---

## 4. 인프라 서비스 실행 (Docker)

Docker Desktop이 실행 중인 상태에서 아래 명령을 실행합니다.

```bash
docker compose up -d
```

다음 컨테이너가 실행됩니다.

| 컨테이너 | 이미지 | 포트 | 용도 |
|----------|--------|------|------|
| `smartoffice-mysql` | mysql:8.0 | 3306 | 메인 데이터베이스 |
| `smartoffice-redis` | redis:7 | 6379 | Refresh Token 저장 |
| `smartoffice-mosquitto` | eclipse-mosquitto:2 | 1883 | MQTT 브로커 (IoT 연동) |

### 컨테이너 상태 확인

```bash
docker compose ps
```

모든 컨테이너의 상태가 `running`이어야 합니다.

### 컨테이너 중지

```bash
docker compose down
```

데이터 볼륨까지 초기화하려면:

```bash
docker compose down -v
```

---

## 5. 애플리케이션 실행

### macOS / Linux

```bash
./gradlew bootRun
```

### Windows (PowerShell)

```powershell
./gradlew.bat bootRun
```

> 기본 프로필은 `local`로 설정되어 있습니다 (`application.yml: spring.profiles.active: local`).
> Flyway가 자동으로 DB 마이그레이션(V1~V4)을 실행합니다.

### 실행 성공 확인

콘솔에 아래 메시지가 출력되면 정상 기동입니다.

```
Started SmartofficeServerApplication in X.XXX seconds
```

---

## 6. Swagger UI 접속

브라우저에서 아래 URL을 엽니다.

```
http://localhost:8080/swagger-ui.html
```

인증이 필요한 API는 우측 상단 **Authorize** 버튼에 아래 형식으로 JWT를 입력합니다.

```
Bearer <액세스 토큰>
```

> 액세스 토큰은 `POST /api/v1/auth/login` 응답의 `data.accessToken` 값입니다.

---

## 7. 프론트엔드·앱에서 로컬 백엔드 연결

### React (웹)

`.env.local` 또는 환경 설정 파일에서 API Base URL을 설정합니다.

```
VITE_API_BASE_URL=http://localhost:8080
```

### Flutter (앱)

Android 에뮬레이터에서 로컬 서버에 접근할 때는 `localhost` 대신 `10.0.2.2`를 사용합니다.

```dart
const baseUrl = 'http://10.0.2.2:8080';
```

실제 기기에서 테스트할 경우 PC와 동일 Wi-Fi 네트워크에 연결 후 PC의 로컬 IP(예: `192.168.x.x`)를 사용합니다.

---

## 8. 테스트 로그인 엔드포인트

> **TODO**: V2~V3 Flyway 시드 데이터 기반의 테스트 계정 정보를 여기에 추가 예정.
>
> - 관리자 계정 (ADMIN 역할)
> - 일반 직원 계정 (EMPLOYEE 역할)
> - 로그인 요청 예시 (`POST /api/v1/auth/login`)

---

## 9. 자주 발생하는 오류 및 해결법

### 오류: `Failed to obtain JDBC Connection`

**원인**: MySQL 컨테이너가 아직 준비되지 않았거나 실행되지 않음.

**해결**:
```bash
docker compose ps          # 컨테이너 상태 확인
docker compose up -d mysql # MySQL만 재시작
```

컨테이너는 떠 있지만 DB 초기화 중일 수 있습니다. 약 10초 기다린 후 재시도합니다.

---

### 오류: `Flyway Migration failed` (checksum mismatch)

**원인**: 이미 적용된 마이그레이션 파일이 로컬에서 수정됨.

**해결**: 개발 환경에서만 허용되는 repair 명령을 실행합니다.
```bash
./gradlew flywayRepair
./gradlew bootRun
```

---

### 오류: `Cannot connect to Redis`

**원인**: Redis 컨테이너가 실행되지 않음.

**해결**:
```bash
docker compose up -d redis
```

---

### 오류: `WeakKeyException` (JWT secret too short)

**원인**: `application-local.yml`의 `jwt.secret`이 32자 미만.

**해결**: `jwt.secret` 값을 32자 이상의 문자열로 변경합니다.

---

### 오류: `Port 3306 is already in use`

**원인**: 호스트에 MySQL이 이미 로컬 설치되어 실행 중.

**해결**: 기존 MySQL 서비스를 중지하거나, `docker-compose.yml`의 포트를 변경합니다.

```yaml
ports:
  - "3307:3306"   # 호스트 포트를 3307로 변경
```

이 경우 `application-local.yml`의 DB URL도 함께 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/smartoffice
```
