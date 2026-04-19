---
description: 레이어별 코드를 구현합니다. 사용법: /implement <도메인명> [domain|application|presentation|infrastructure|all]
---

다음 구현 요청을 처리하세요: $ARGUMENTS

## 참조 컨벤션

구현 전 아래 파일을 반드시 읽고 모든 규칙을 적용하세요:
- `.claude/rules/kotlin-conventions.md`
- `.claude/rules/spring-conventions.md`
- `CLAUDE.md` — 아키텍처 원칙, 레이어 의존성, 금지 사항

## 구현 절차

1. `$ARGUMENTS`에서 도메인명과 대상 레이어 파악
2. `src/main/kotlin/com/example/demo/` 하위 기존 파일 탐색하여 컨텍스트 파악
3. 레이어 미지정 시 `all` 처리 — domain → application → infrastructure → presentation 순서
4. 읽은 규칙을 적용하여 각 레이어 파일 생성/수정

## 레이어별 생성 파일 목록

### domain
- `domain/model/{Domain}.kt` — Aggregate Root
- `domain/model/{Domain}Id.kt` — Value Object (식별자)
- `domain/event/{Domain}CreatedEvent.kt` — Domain Event
- `domain/exception/{Domain}NotFoundException.kt` — Domain Exception
- `domain/repository/{Domain}Repository.kt` — Repository 인터페이스

### application
- `application/dto/{Action}Request.kt` — 요청 DTO (예: `CreateOrderRequest`)
- `application/dto/{Action}Response.kt` — 응답 DTO (예: `CreateOrderResponse`)
- `application/service/{Domain}Service.kt` — Application Service

### infrastructure
- `infrastructure/persistence/{domain}/{Domain}JpaEntity.kt` — JPA Entity
- `infrastructure/persistence/{domain}/{Domain}JpaRepository.kt` — Spring Data JPA 인터페이스
- `infrastructure/persistence/{domain}/{Domain}RepositoryImpl.kt` — Repository 구현체

### presentation
- `presentation/{domain}/{Domain}Controller.kt` — REST Controller

## 출력

생성한 파일 목록과 각 파일의 역할을 요약하여 알려주세요.
파일 경로는 `src/main/kotlin/com/example/demo/` 기준 상대경로로 표시하세요.
