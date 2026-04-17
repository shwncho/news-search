---
description: 레이어별 코드를 구현합니다. kotlin-conventions, spring-conventions을 준수합니다. 사용법: /implement <도메인명> [domain|application|presentation|infrastructure|all]
---

다음 구현 요청을 처리하세요: $ARGUMENTS

## 참조 컨벤션

구현 전 아래 두 파일을 반드시 읽고 규칙을 적용하세요:
- `.claude/skills/kotlin-conventions.md` — Kotlin 코드 스타일 규칙
- `.claude/skills/spring-conventions.md` — Spring 컴포넌트 설계 규칙

## 구현 전 수행할 작업

1. `$ARGUMENTS`에서 도메인명과 대상 레이어를 파악
2. `src/main/kotlin/com/example/demo/` 하위에서 기존 관련 파일을 탐색하여 컨텍스트 파악
3. 레이어가 지정되지 않으면 `all`로 처리 (domain → application → infrastructure → presentation 순서)

## 레이어별 생성 파일 목록

### domain
- `domain/model/{Domain}.kt` — Aggregate Root
- `domain/model/{Domain}Id.kt` — Value Object (식별자)
- `domain/event/{Domain}CreatedEvent.kt` — Domain Event
- `domain/exception/{Domain}NotFoundException.kt` — Domain Exception
- `domain/repository/{Domain}Repository.kt` — Repository 인터페이스

### application
- `application/dto/{Domain}Command.kt` — 상태 변경 입력 DTO
- `application/dto/{Domain}Response.kt` — 응답 DTO (presentation 공유)
- `application/service/{Domain}Service.kt` — Application Service

### infrastructure
- `infrastructure/persistence/{domain}/{Domain}JpaEntity.kt` — JPA Entity
- `infrastructure/persistence/{domain}/{Domain}JpaRepository.kt` — Spring Data JPA 인터페이스
- `infrastructure/persistence/{domain}/{Domain}RepositoryImpl.kt` — Repository 구현체

### presentation
- `presentation/{domain}/{Domain}Controller.kt` — REST Controller

## 구현 규칙 (반드시 준수)

### Kotlin 규칙 (kotlin-conventions.md 기준)
- `val` 우선, `!!` 연산자 금지
- `private constructor` + `companion object { fun create(...) }` 팩토리 패턴
- `require`·`check` 로 불변식 검증
- 내부 컬렉션은 `MutableList` 사용, 외부에는 `List`로만 공개
- `@JvmInline value class`로 식별자 Value Object 정의

### Spring 규칙 (spring-conventions.md 기준)
- `@Service` 어노테이션만, UseCase 인터페이스 생성 금지
- 클래스 레벨 `@Transactional`, 조회 메서드는 `@Transactional(readOnly = true)`
- 생성자 주입 (`private val`)
- Command DTO에 `@field:NotBlank` 등 Bean Validation 어노테이션
- Response DTO에 `companion object { fun from(domain) }` 패턴

### 아키텍처 규칙 (CLAUDE.md 기준)
- 도메인 레이어에 Spring/JPA 어노테이션 금지
- Application Service는 `domain/repository` 인터페이스에만 의존
- 모든 DTO는 `application/dto/`에 위치
- `presentation/`에 별도 DTO 생성 금지
- 도메인 객체를 Service 외부로 직접 반환 금지

## 출력

생성한 파일 목록과 각 파일의 역할을 요약하여 알려주세요.
파일 경로는 `src/main/kotlin/com/example/demo/` 기준 상대경로로 표시하세요.
