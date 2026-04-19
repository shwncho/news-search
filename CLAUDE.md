# CLAUDE.md

## 프로젝트 개요

- **언어**: Kotlin 2.0.21 / Java 21
- **프레임워크**: Spring Boot 3.4.1
- **아키텍처**: DDD + Layered Clean Architecture
- **빌드**: Gradle (Kotlin DSL) / **린터**: ktlint 12.x

코드 작성 컨벤션은 `.claude/rules/` 하위 파일을 참조한다:
- Kotlin 코드 스타일 → `.claude/rules/kotlin-conventions.md`
- Spring 컴포넌트 설계 → `.claude/rules/spring-conventions.md`
- 테스트 작성 규칙 → `.claude/rules/testing-conventions.md`

---

## 디렉토리 구조

```
src/main/kotlin/com/example/demo/
├── domain/
│   ├── model/          # Aggregate Root, Entity, Value Object
│   ├── event/          # Domain Event
│   ├── exception/      # Domain Exception
│   └── repository/     # Repository 인터페이스 (도메인 레이어 소유)
├── application/
│   ├── dto/            # Request, Response DTO 중앙 관리
│   └── service/        # Application Service
├── presentation/
│   └── {domain}/       # Controller (DTO는 application/dto 직접 사용)
├── infrastructure/
│   └── persistence/
│       └── {domain}/   # JPA Entity, Spring Data JPA, RepositoryImpl
└── config/             # Spring 설정
```

---

## 레이어 간 의존성 규칙

```
presentation  →  application/service, application/dto
application   →  domain/model, domain/repository
infrastructure →  domain/model, domain/repository (구현)
domain        →  없음 (순수 Kotlin, 외부 의존 금지)
```

### 절대 금지 방향

| 위반 레이어 | 금지 import |
|------------|------------|
| `domain` | `org.springframework.*`, `jakarta.persistence.*`, `application.*`, `presentation.*`, `infrastructure.*` |
| `application` | `jakarta.persistence.*`, `presentation.*`, `infrastructure.*` |
| `presentation` | `infrastructure.*` (도메인 객체 직접 반환 금지) |
| `infrastructure` | `application.*`, `presentation.*` |

---

## 레이어 간 데이터 전달 규칙

1. **Presentation → Application**: `application/dto`의 Request DTO로 전달
2. **Application → Presentation**: `application/dto`의 Response DTO로 반환 (도메인 객체 직접 노출 금지)
3. **Application → Domain**: 도메인 객체 및 Value Object 직접 사용
4. **Infrastructure → Domain**: JPA Entity ↔ Domain 객체 변환은 Infrastructure 내부에서만 수행

```
HTTP Request
    ↓ (Request DTO)
Controller
    ↓ (Request DTO)
Application Service
    ↓ (Domain Object / Value Object)
Domain Model ←→ Repository Interface
                    ↑ (implements)
             RepositoryImpl (Infrastructure)
                    ↓ (JPA Entity ↔ Domain 변환)
              Database
    ↑ (Response DTO)
Application Service
    ↑ (Response DTO)
Controller
    ↑ (HTTP Response)
```

### DTO 배치 원칙

- 모든 Request, Response DTO는 `application/dto/`에 위치
- `presentation/` 레이어에 별도 DTO 클래스 생성 금지
- DTO 네이밍: `{Action}Request` / `{Action}Response` (예: `CreateOrderRequest`, `CreateOrderResponse`)
- Response DTO는 `companion object { fun from(domain) }` 패턴으로 변환
- Request DTO는 입력 검증 어노테이션(`@field:NotBlank` 등) 포함

---

## DDD 설계 원칙

### Aggregate

- Aggregate Root만 외부에서 직접 접근; 내부 Entity는 Root를 통해서만 조작
- `private constructor` + `companion object`의 `create` / `reconstruct` 팩토리 메서드 패턴 강제
  - `create`: 신규 생성 (불변식 검증, Domain Event 발행)
  - `reconstruct`: 영속성 복원 (검증·이벤트 없이 상태만 재구성)
- 생성/변경 시 `require`·`check`로 불변식 검증
- 내부 컬렉션은 외부에 mutable하게 노출 금지 (`List` 타입으로만 공개)
- Aggregate 간 참조는 ID(Value Object)로만 — 다른 Aggregate 객체를 직접 참조 금지
- 하나의 트랜잭션에서 하나의 Aggregate만 수정

### Value Object

- 동등성이 값으로 결정되는 개념에 사용
- 반드시 불변(`val` 필드만)
- 단일 식별자는 `@JvmInline value class`로, 복합 값은 `data class`로

### Domain Event

- 도메인 레이어(`domain/event/`)에서 정의
- `DomainEvent` 마커 인터페이스 구현
- Aggregate 내부에서 이벤트를 생성하고 Application Service에서 발행
- 실제 Domain Event가 존재할 때만 `DomainEvent` 마커 인터페이스 및 이벤트 클래스 생성

### Repository 인터페이스

- `domain/repository/`에 위치 (도메인 레이어 소유)
- 프레임워크 의존 없는 순수 Kotlin 인터페이스
- 구현체(`RepositoryImpl`)는 `infrastructure/persistence/{domain}/`에 위치

---

## Domain-Infrastructure 분리 원칙

1. **도메인 객체에 JPA 어노테이션 금지**: `@Entity`, `@Column`, `@Id` 등은 Infrastructure JPA Entity에만
2. **JPA Entity는 Infrastructure 전용**: `domain/model/`의 클래스와 별개로 `infrastructure/persistence/`에 JPA Entity 별도 정의
3. **변환 책임은 Infrastructure**: `JpaEntity.toDomain()` / `JpaEntity.from(domain)` 패턴으로 Infrastructure 내에서만 변환
4. **Repository 인터페이스는 Domain이 소유**: Application Service는 `domain/repository/` 인터페이스에만 의존, Infrastructure 구현체를 직접 참조하지 않음

---

## Application Service 설계 원칙

- UseCase 인터페이스 생성 금지 — `@Service` 클래스로 직접 구현
- 비즈니스 로직은 도메인 객체에 위임, Service는 흐름(오케스트레이션)만 담당
- 상태 변경 메서드: `@Transactional` (클래스 레벨 기본값)
- 조회 전용 메서드: `@Transactional(readOnly = true)` 명시
- 도메인 객체를 Service 밖으로 직접 반환 금지 — Response DTO로 변환

---

## 금지 사항

| 금지 | 대안 |
|------|------|
| `!!` 연산자 | 엘비스 연산자(`?:`) 또는 도메인 예외 |
| 도메인 레이어에 Spring/JPA 어노테이션 | Infrastructure 레이어에 위치 |
| Application Service에 UseCase 인터페이스 | Service 클래스 직접 구현 |
| `presentation/`에 별도 DTO 클래스 | `application/dto` 사용 |
| Application Service에서 infrastructure 직접 참조 | domain/repository 인터페이스 사용 |
| Controller에 비즈니스 로직 | Application Service에 위임 |
| `Any` 타입 남용 | 명확한 타입 정의 |

---

## 빌드 및 검증

```bash
./gradlew ktlintCheck    # 스타일 검사
./gradlew ktlintFormat   # 스타일 자동 수정
./gradlew test           # 전체 테스트
./gradlew build          # 빌드
```

---

## 커스텀 명령어

| 명령어 | 설명 |
|--------|------|
| `/implement <도메인명> [레이어]` | 레이어별 코드 구현 (kotlin/spring 컨벤션 준수) |
| `/gen-test <파일경로>` | 레이어에 맞는 테스트 코드 생성 |
| `/review [경로]` | 아키텍처·DDD·컨벤션 전체 리뷰 |
