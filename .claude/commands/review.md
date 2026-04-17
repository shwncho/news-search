---
description: 아키텍처, DDD 원칙, 코드 컨벤션 전체를 리뷰합니다. 사용법: /review [파일경로 또는 디렉토리] (미지정 시 전체 분석)
---

다음 대상을 리뷰하세요: $ARGUMENTS

## 참조 문서

리뷰 전 아래 파일을 반드시 읽고 기준으로 사용하세요:
- `CLAUDE.md` — 아키텍처 원칙, 레이어 의존성 규칙, DDD 원칙, 금지 사항
- `.claude/skills/kotlin-conventions.md` — Kotlin 코드 스타일 규칙
- `.claude/skills/spring-conventions.md` — Spring 컴포넌트 설계 규칙
- `.claude/skills/testing-conventions.md` — 테스트 작성 규칙

## 분석 절차

### 1. 대상 파악
- `$ARGUMENTS`가 비어 있으면 `src/main/kotlin/` 전체 분석
- 경로가 지정되면 해당 파일/디렉토리만 분석
- Glob/Grep으로 대상 파일 목록 수집

### 2. 레이어 의존성 검사 (CLAUDE.md 기준)

각 파일의 import 문을 분석하여 방향 위반을 찾는다:

| 파일 위치 | 금지 import 패턴 | 심각도 |
|----------|----------------|--------|
| `domain/` | `org.springframework.*`, `jakarta.persistence.*`, `*.application.*`, `*.presentation.*`, `*.infrastructure.*` | CRITICAL |
| `application/` | `jakarta.persistence.*`, `*.presentation.*`, `*.infrastructure.*` | CRITICAL |
| `presentation/` | `*.infrastructure.*` | MAJOR |
| `infrastructure/` | `*.application.*`, `*.presentation.*` | MAJOR |

### 3. DDD 원칙 검사 (CLAUDE.md 기준)

**Aggregate**:
- `private constructor` 사용 여부
- `require`·`check` 불변식 검증 여부
- 내부 컬렉션 캡슐화 (`MutableList` 외부 노출 금지)
- Aggregate 간 참조가 ID(Value Object)로만 이루어지는지

**Value Object**:
- `@JvmInline value class` 또는 `data class` 사용 여부
- `val` 필드만 사용 (불변 보장)

**Repository**:
- 인터페이스가 `domain/repository/`에 위치하는지
- 구현체가 `infrastructure/persistence/`에 위치하는지

**Domain Event**:
- `domain/event/`에 위치하는지
- `DomainEvent` 마커 인터페이스 구현 여부

### 4. DTO 관리 검사 (CLAUDE.md 기준)

- `application/dto/` 외 위치에 Command/Query/Response DTO가 없는지
- `presentation/` 하위에 별도 DTO 클래스가 없는지
- Command DTO에 Bean Validation 어노테이션이 있는지
- Response DTO에 `companion object { fun from(domain) }` 패턴이 있는지

### 5. Kotlin 코드 스타일 검사 (kotlin-conventions.md 기준)

- `!!` 연산자 사용 여부
- `var` 남용 여부 (필요 없는 가변 선언)
- `when` 표현식의 exhaustive 처리
- `MutableList` 등 mutable 컬렉션의 외부 노출 여부
- ktlint 규칙 위반 (들여쓰기 4칸, 최대 줄 길이 120자)

### 6. Spring 컴포넌트 설계 검사 (spring-conventions.md 기준)

- Application Service에 UseCase 인터페이스가 없는지
- 조회 메서드에 `@Transactional(readOnly = true)` 적용 여부
- 생성자 주입 사용 여부
- Controller에 비즈니스 로직이 없는지
- 도메인 예외 클래스에 Spring 어노테이션이 없는지

### 7. 테스트 검사 (testing-conventions.md 기준)

- 각 레이어에 맞는 테스트 유형 사용 여부
- Application Service 테스트에서 MockK 사용 여부
- Controller 테스트에서 `@MockkBean` 사용 여부
- Fixture 패턴 사용 여부
- AAA 패턴(`// given`, `// when`, `// then`) 사용 여부

## 출력 형식

```
## 코드 리뷰 결과

### 심각도별 이슈

**[CRITICAL]** 아키텍처 의존성 위반 또는 런타임 오류 가능성
- `파일경로:줄번호` — 위반 내용

**[MAJOR]** DDD 원칙 위반 또는 설계 규칙 불일치
- `파일경로:줄번호` — 위반 내용

**[MINOR]** Kotlin 스타일 또는 컨벤션 개선
- `파일경로:줄번호` — 개선 내용

### 레이어별 준수 현황

| 레이어 | 의존성 | DDD | Kotlin | Spring | 테스트 |
|--------|--------|-----|--------|--------|--------|
| domain | ✅/❌ | ✅/❌ | ✅/❌ | — | ✅/❌ |
| application | ✅/❌ | — | ✅/❌ | ✅/❌ | ✅/❌ |
| presentation | ✅/❌ | — | ✅/❌ | ✅/❌ | ✅/❌ |
| infrastructure | ✅/❌ | — | ✅/❌ | ✅/❌ | ✅/❌ |

### 잘 지켜진 항목
- ...

### 개선 제안 코드
```kotlin
// Before
// After
```

### 우선 개선 순서
1. [CRITICAL] ...
2. [MAJOR] ...
3. [MINOR] ...
```
