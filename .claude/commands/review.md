---
description: 아키텍처, DDD 원칙, 코드 컨벤션 전체를 리뷰합니다. 사용법: /review [파일경로 또는 디렉토리] (미지정 시 전체 분석)
---

다음 대상을 리뷰하세요: $ARGUMENTS

## 참조 문서

리뷰 전 아래 파일을 반드시 읽고 기준으로 사용하세요:
- `CLAUDE.md` — 아키텍처 원칙, 레이어 의존성 규칙, DDD 원칙, 금지 사항
- `.claude/rules/kotlin-conventions.md` — Kotlin 코드 스타일 규칙
- `.claude/rules/spring-conventions.md` — Spring 컴포넌트 설계 규칙
- `.claude/rules/testing-conventions.md` — 테스트 작성 규칙

## 분석 절차

### 1. 대상 파악
- `$ARGUMENTS`가 비어 있으면 `src/main/kotlin/` 전체 분석
- 경로가 지정되면 해당 파일/디렉토리만 분석
- Glob/Grep으로 대상 파일 목록 수집

### 2. 분석 기준

`.claude/agents/reviewer.md`의 체크리스트를 기준으로 분석한다.
분석 항목: 레이어 의존성 → DDD 원칙 → DTO 관리 → Application Service → Controller → Kotlin 스타일 → 테스트

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
