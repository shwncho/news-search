---
description: 대상 파일의 레이어에 맞는 테스트 코드를 생성합니다. 사용법: /gen-test <파일경로 또는 도메인명>
---

다음 대상에 대한 테스트를 생성하세요: $ARGUMENTS

## 참조 컨벤션

테스트 생성 전 아래 파일을 반드시 읽고 모든 규칙을 적용하세요:
- `.claude/rules/testing-conventions.md` — 레이어별 테스트 전략, 작성 규칙, Fixture 패턴, 코드 템플릿

## 생성 절차

1. `$ARGUMENTS`에서 대상 파일 경로 또는 도메인명 파악
2. 대상 파일을 Read로 읽어 클래스 구조, 메서드, 의존성 분석
3. 파일 경로(`domain/`, `application/service/`, `presentation/`, `infrastructure/`)로 레이어 판별
4. 기존 테스트 파일이 있으면 누락된 케이스만 추가
5. testing-conventions.md의 해당 레이어 규칙에 따라 테스트 작성

## 테스트 파일 위치

소스 파일과 동일한 패키지 구조를 `src/test/kotlin/`에 미러링:

```
src/main/kotlin/com/example/demo/{layer}/{...}/{Class}.kt
    → src/test/kotlin/com/example/demo/{layer}/{...}/{Class}Test.kt
```

Fixture:
```
src/test/kotlin/com/example/demo/fixture/{Domain}Fixture.kt
```

## 출력

생성한 테스트 파일 경로와 테스트 케이스 목록을 요약하여 알려주세요.
