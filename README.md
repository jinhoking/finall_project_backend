# 🛡️ ECP SYSTEM - Backend Technical Specification

사내 통합 관리 시스템(Empower Corporate Platform)의 백엔드 시스템은 **Spring Boot 3.4**를 기반으로 구축되었으며, 안정적인 데이터 관리와 보안을 최우선으로 설계되었습니다.

## 🛠️ 기술 스택 (Technical Stack)

- **Framework:** Spring Boot 3.4.1
- **Language:** Java 17
- **Security:** Spring Security, JWT (JSON Web Token)
- **Database:** MySQL 8.0, Spring Data JPA
- **Communication:** WebSocket (STOMP), Axios (REST API)
- **AOP:** AspectJ (Security Audit Logging)
- **Encryption:** AES-256-ECB (Data Privacy)
- **Build Tool:** Gradle

## 📂 프로젝트 구조 (Project Structure)

```text
src/main/java/com/boot/security
├── annotation      # 커스텀 어노테이션 (@AuditLog)
├── aop             # 공통 관심사 처리 (Security Audit Aspect)
├── config          # 설정 클래스 (Security, WebSocket, Password, Encryption)
├── controller      # REST API 컨트롤러 레이어
├── dto             # 데이터 전송 객체 (Request/Response)
├── entity          # JPA 엔티티 레이어 (Database Mapping)
├── enums           # 시스템 공통 Enum (DocumentStatus, UserStatus 등)
├── provider        # 보안 라이브러리 (JwtTokenProvider, Filter)
├── repository      # 데이터 접근 레이어 (Spring Data JPA)
├── role            # 사용자 권한 정의 (UserRole)
└── service         # 핵심 비즈니스 로직 레이어
```

## 🔑 핵심 구현 상세 (Key Implementations)

### 1. 보안 및 인증 (Security & JWT)
* **Stateless Auth:** 모든 API 요청은 `JwtAuthenticationFilter`를 통해 토큰 유효성을 검증하며, 유효한 토큰일 경우 `SecurityContext`에 인증 정보를 저장합니다.
* **CORS Policy:** 특정 도메인(`http://3.36.75.235`, `http://localhost:5173`)에서의 접근을 허용하여 프론트엔드와 안전하게 통신합니다.

### 2. 데이터 보호 (Data Encryption)
* **AES-256 암호화:** `DataEncryptor`를 구현하여 개인정보(이름, 이메일, 전화번호, 주소)를 DB 저장 시 자동 암호화합니다.
* **Transparent Encryption:** JPA `AttributeConverter`를 사용하여 서비스 로직의 수정 없이 필드 레벨에서 암복호화를 투명하게 처리합니다.

### 3. 실시간 보안 감사 (AOP Audit Logging)
* **AOP 적용:** 비즈니스 로직과 로깅 로직을 분리하기 위해 AspectJ를 사용합니다. 
* **Audit Logging:** `@AuditLog` 어노테이션이 부착된 메서드의 실행 시간, 인자, 성공 여부를 가로채어 실시간 보안 감사 로그를 생성합니다.

### 4. 비즈니스 도메인 로직
* **전자결재:** 기안서-결재자-파일 간의 복합적인 관계를 JPA `CascadeType.ALL`로 관리하며, 모든 결재자의 승인 여부에 따른 상태 전이 로직을 포함합니다.
* **WebSocket 채팅:** STOMP 브로커를 통해 저지연(Low-latency) 1:1 채팅을 구현하였으며, 최신 메시지 기반의 채팅방 정렬 쿼리를 최적화했습니다.
* **프로젝트 진척도:** 각 개발 단계(UI, React, Java, DB)별 수치를 계산하여 전체 프로젝트 공정률을 산출하는 연산 로직을 포함합니다.

## 💾 데이터베이스 구성 (ERD 요약)
* **Core:** Users, Departments (인사/조직)
* **Approval:** Documents, Approvals, DocumentObservers (전자결재)
* **Assets:** Assets, AssetHistory (자산 관리)
* **Dev:** Projects, ProjectDetails, ProjectIssues (개발 관리)
* **System:** ChatMessages, Notices, Schedules (협업/공지)

## 🚀 실행 환경 설정 (application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/securesync_db
  jpa:
    hibernate:
      ddl-auto: update
jwt:
  secret: ${JWT_SECRET_KEY}
```

---
*본 문서는 ECP SYSTEM 백엔드 소스 코드를 바탕으로 자동 생성된 기술 명세서입니다.*
