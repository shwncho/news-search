---
description: 대상 파일의 레이어에 맞는 테스트 코드를 생성합니다. 사용법: /gen-test <파일경로 또는 도메인명>
---

다음 대상에 대한 테스트를 생성하세요: $ARGUMENTS

## 참조 컨벤션

테스트 생성 전 아래 파일을 반드시 읽고 규칙을 적용하세요:
- `.claude/skills/testing-conventions.md` — 레이어별 테스트 작성 규칙

## 생성 전 수행할 작업

1. `$ARGUMENTS`에서 대상 파일 경로 또는 도메인명 파악
2. 대상 파일을 Read로 읽어 클래스 구조, 메서드, 의존성 분석
3. 파일 경로(`domain/`, `application/service/`, `presentation/`, `infrastructure/`)를 보고 레이어 판별
4. 이미 존재하는 테스트 파일이 있으면 누락된 케이스만 추가

## 레이어별 테스트 전략

### domain/ → 단위 테스트
- 프레임워크 없는 순수 Kotlin 테스트
- Aggregate 불변식, 상태 전이, Domain Event 발행 시나리오
- `@Test` + JUnit 5

### application/service/ → 단위 테스트 (MockK)
- `@ExtendWith(MockKExtension::class)`
- Repository는 `@MockK`로 목킹
- Service 생성자 주입 (`@BeforeEach setUp`)
- `verify(exactly = N)` 으로 호출 횟수 검증

### presentation/ → 슬라이스 테스트
- `@WebMvcTest(Controller::class)`
- Service는 `@MockkBean`
- MockMvc로 HTTP 요청/응답 검증
- 성공(2xx) + 입력 오류(400) + 도메인 예외(404/409) 케이스

### infrastructure/persistence/ → 슬라이스 테스트
- `@DataJpaTest`
- RepositoryImpl 직접 인스턴스화
- 도메인 객체 ↔ JPA Entity 변환 정확성 + CRUD 시나리오

## 테스트 작성 규칙

- 테스트 함수명: 백틱 + 한국어 또는 영어 서술형
- AAA 패턴 주석 (`// given`, `// when`, `// then`)
- Happy path + Edge case + Error case 모두 작성
- Fixture 오브젝트로 테스트 데이터 관리 (없으면 함께 생성)

## Fixture 파일 위치

```
src/test/kotlin/com/example/demo/fixture/
└── {Domain}Fixture.kt      # 도메인 객체 Fixture
└── {Domain}CommandFixture.kt
└── {Domain}ResponseFixture.kt
```

## 테스트 파일 위치

생성 대상 파일과 동일한 패키지 구조를 `src/test/kotlin/`에 미러링:

```
src/main/kotlin/com/example/demo/domain/model/Order.kt
    → src/test/kotlin/com/example/demo/domain/model/OrderTest.kt

src/main/kotlin/com/example/demo/application/service/OrderService.kt
    → src/test/kotlin/com/example/demo/application/service/OrderServiceTest.kt

src/main/kotlin/com/example/demo/presentation/order/OrderController.kt
    → src/test/kotlin/com/example/demo/presentation/order/OrderControllerTest.kt

src/main/kotlin/com/example/demo/infrastructure/persistence/order/OrderRepositoryImpl.kt
    → src/test/kotlin/com/example/demo/infrastructure/persistence/order/OrderRepositoryImplTest.kt
```

## 출력

생성한 테스트 파일 경로와 테스트 케이스 목록을 요약하여 알려주세요.
