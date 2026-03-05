# ⚙️ FACUBE ECP - Backend Technical Specification

사내 통합 관리 시스템(**Enterprise Control Platform**)의 백엔드 시스템은 **Spring Boot 3.4**를 기반으로 구축되었으며, 안정적인 데이터 관리와 보안을 최우선으로 설계되었습니다.

---

## 🛠️ 기술 스택 (Technical Stack)

* **Framework:** Spring Boot 3.4.1
* **Language:** Java 17 (OpenJDK)
* **Security:** Spring Security, JWT (JSON Web Token)
* **Database:** MySQL 8.0, Spring Data JPA
* **Communication:** WebSocket (STOMP), Axios (REST API)
* **AOP:** AspectJ (Security Audit Logging)
* **Encryption:** AES-256-ECB (Data Privacy)
* **Build Tool:** Gradle

---

## 📂 프로젝트 구조 (Project Structure)

```text
src/main/java/com/boot/security
├── annotation      # 커스텀 어노테이션 (@AuditLog)
├── aop             # 공통 관심사 처리 (Security Audit Aspect)
├── config          # 설정 클래스 (Security, WebSocket, Encryption)
├── controller      # REST API 컨트롤러 레이어
├── dto             # 데이터 전송 객체 (Request/Response)
├── entity          # JPA 엔티티 레이어 (Database Mapping)
├── provider        # 보안 라이브러리 (JwtTokenProvider, Filter)
├── repository      # 데이터 접근 레이어 (Spring Data JPA)
└── service         # 핵심 비즈니스 로직 레이어
```

---

## 🔑 핵심 구현 상세 (Key Implementations)

### 1. 보안 및 인증 (Security & JWT)
* **Stateless Auth:** 모든 API 요청은 `JwtAuthenticationFilter`를 통해 토큰 유효성을 검증합니다.
* **CORS Policy:** 프론트엔드 도메인(`ecpsystem.site`) 및 로컬 환경에 대한 정밀한 접근 제어를 적용했습니다.

### 2. 데이터 보호 (Data Encryption)
* **AES-256 암호화:** `AttributeConverter`를 사용하여 개인정보(이름, 이메일 등)를 DB 저장 시 자동 암복호화합니다.
* **BCrypt:** 비밀번호는 `BCryptPasswordEncoder`를 통해 단방향 해시 암호화됩니다.

### 3. 실시간 보안 감사 (AOP Audit Logging)
* **AOP 적용:** `@AuditLog` 어노테이션을 활용해 주요 메서드의 실행 이력을 가로채 실시간 감사 로그를 생성합니다.

---

## 🚀 Getting Started (실행 방법)

### 1. 환경 설정 (`src/main/resources/application.yml`)
데이터베이스 정보와 JWT 시크릿 키를 설정하세요.
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/securesync_db
    username: your_username
    password: your_password
jwt:
  secret: ${JWT_SECRET_KEY_MIN_32_CHARS}
```

### 2. 빌드 및 실행
```bash
# 실행 권한 부여
chmod +x gradlew

# 프로젝트 빌드
./gradlew clean build

# 서버 실행 (백그라운드)
nohup java -jar build/libs/main_securesync-0.0.1-SNAPSHOT.jar &
```

---

## 🆘 Troubleshooting (에러 조치 방법)

### Q1. `./gradlew` 실행 시 'Permission Denied' 발생
* **해결:** `chmod +x gradlew` 명령어를 실행하세요.

### Q2. 자바 버전 관련 빌드 에러
* **해결:** `JAVA_HOME` 환경변수가 JDK 17로 설정되어 있는지 확인하세요.

### Q3. "Port 8080 was already in use"
* **해결:** `fuser -k 8080/tcp` 명령어로 기존 프로세스를 종료하세요.

---
*본 문서는 ECP SYSTEM 백엔드 소스 코드를 바탕으로 작성된 기술 명세서입니다.*
