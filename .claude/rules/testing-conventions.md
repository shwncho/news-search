# 테스트 작성 컨벤션

이 파일은 레이어별 테스트 작성 규칙을 정의한다.
`gen-test.md`와 `reviewer.md`가 이 규칙을 참조한다.

---

## 테스트 전략 개요

| 레이어 | 테스트 유형 | 주요 도구 |
|--------|------------|----------|
| `domain/` | 단위 테스트 | JUnit 5, AssertJ (또는 Kotest) |
| `application/service/` | 단위 테스트 | MockK (`@ExtendWith(MockKExtension::class)`) |
| `presentation/` | 슬라이스 테스트 | `@WebMvcTest`, MockMvc, `@MockkBean` |
| `infrastructure/persistence/` | 슬라이스 테스트 | `@DataJpaTest` |
| E2E | 통합 테스트 | `@SpringBootTest` |

---

## 공통 규칙

### 테스트 함수 네이밍
```kotlin
// 백틱 + 한국어 서술형 권장
@Test
fun `주문 생성 시 저장소에 저장하고 Response를 반환해야 한다`() { }


### AAA 패턴 주석
```kotlin
@Test
fun `...`() {
    // given
    val command = ...

    // when
    val result = service.create(command)

    // then
    assertThat(result).isNotNull()
}
```

### Fixture 패턴
테스트 데이터는 `object Fixture`로 중앙 관리한다.

```kotlin
// test/.../fixture/OrderFixture.kt
object OrderFixture {
    fun create(
        id: OrderId = OrderId(UUID.randomUUID()),
        customerId: CustomerId = CustomerId(UUID.randomUUID()),
        status: OrderStatus = OrderStatus.PENDING,
    ): Order = Order.reconstruct(id = id, customerId = customerId, status = status)
}

object CreateOrderRequestFixture {
    fun create(
        customerId: String = UUID.randomUUID().toString(),
    ) = CreateOrderRequest(customerId = customerId)
}

object OrderResponseFixture {
    fun create(
        id: UUID = UUID.randomUUID(),
        customerId: UUID = UUID.randomUUID(),
    ) = OrderResponse(id = id, customerId = customerId, status = "PENDING", itemCount = 0)
}
```

---

## Domain 단위 테스트

```kotlin
class OrderTest {

    @Test
    fun `주문 생성 시 PENDING 상태여야 한다`() {
        // given
        val customerId = CustomerId(UUID.randomUUID())

        // when
        val order = Order.create(customerId)

        // then
        assertThat(order.status).isEqualTo(OrderStatus.PENDING)
        assertThat(order.customerId).isEqualTo(customerId)
    }

    @Test
    fun `수량이 0 이하인 아이템 추가 시 예외가 발생해야 한다`() {
        // given
        val order = OrderFixture.create()

        // when & then
        assertThatThrownBy { order.addItem(ProductId(UUID.randomUUID()), quantity = 0, price = Money(BigDecimal.TEN, Currency.KRW)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("수량은 0보다 커야 합니다")
    }
}
```

### 규칙
- 프레임워크 의존 없이 순수 Kotlin 테스트
- Aggregate 불변식 검증, 도메인 이벤트 발행, 상태 전이 시나리오 커버
- Happy path + Edge case + Error case 모두 작성

---

## Application Service 단위 테스트

```kotlin
@ExtendWith(MockKExtension::class)
class OrderServiceTest {

    @MockK
    lateinit var orderRepository: OrderRepository

    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderService = OrderService(orderRepository)
    }

    @Test
    fun `주문 생성 시 저장소에 저장하고 Response를 반환해야 한다`() {
        // given
        val request = CreateOrderRequestFixture.create()
        val savedOrder = OrderFixture.create()
        every { orderRepository.save(any()) } returns savedOrder

        // when
        val result = orderService.createOrder(request)

        // then
        verify(exactly = 1) { orderRepository.save(any()) }
        assertThat(result.id).isEqualTo(savedOrder.id.value)
    }

    @Test
    fun `존재하지 않는 ID로 조회 시 OrderNotFoundException이 발생해야 한다`() {
        // given
        val id = UUID.randomUUID().toString()
        every { orderRepository.findById(any()) } returns null

        // when & then
        assertThatThrownBy { orderService.getOrder(id) }
            .isInstanceOf(OrderNotFoundException::class.java)
    }
}
```

### 규칙
- `@ExtendWith(MockKExtension::class)` 사용
- Repository는 `@MockK`로 목킹
- Service 직접 생성자 주입 (`@BeforeEach setUp`)
- UseCase 인터페이스 없으므로 Service 클래스 직접 테스트
- `verify(exactly = N)` 으로 호출 횟수 검증

---

## Presentation Controller 슬라이스 테스트

```kotlin
@WebMvcTest(OrderController::class)
class OrderControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    // Service를 MockkBean으로 처리 (UseCase 인터페이스 없음)
    @MockkBean
    lateinit var orderService: OrderService

    @Test
    fun `POST orders - 성공 시 201과 OrderResponse 반환`() {
        // given
        val request = CreateOrderRequestFixture.create()
        val response = OrderResponseFixture.create()
        every { orderService.createOrder(any()) } returns response

        // when & then
        mockMvc.post("/api/v1/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { exists() }
            jsonPath("$.status") { value("PENDING") }
        }
    }

    @Test
    fun `POST orders - 유효하지 않은 입력 시 400 반환`() {
        // given
        val invalidRequest = mapOf("customerId" to "")

        // when & then
        mockMvc.post("/api/v1/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidRequest)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `GET orders - 존재하지 않는 ID면 404 반환`() {
        // given
        every { orderService.getOrder(any()) } throws OrderNotFoundException("not-found")

        // when & then
        mockMvc.get("/api/v1/orders/not-found")
            .andExpect { status { isNotFound() } }
    }
}
```

### 규칙
- `@WebMvcTest(Controller::class)` — 단일 Controller만 로드
- Service는 `@MockkBean` (spring-mockk 라이브러리)
- `MockMvc`로 HTTP 요청/응답 검증
- 성공(2xx), 입력 오류(400), 도메인 예외(404/409) 케이스 모두 커버

---

## Infrastructure Repository 슬라이스 테스트

```kotlin
@DataJpaTest
class OrderRepositoryImplTest {

    @Autowired
    lateinit var orderJpaRepository: OrderJpaRepository

    private lateinit var orderRepositoryImpl: OrderRepositoryImpl

    @BeforeEach
    fun setUp() {
        orderRepositoryImpl = OrderRepositoryImpl(orderJpaRepository)
    }

    @Test
    fun `저장 후 ID로 조회하면 동일한 도메인 객체를 반환해야 한다`() {
        // given
        val order = OrderFixture.create()

        // when
        val saved = orderRepositoryImpl.save(order)
        val found = orderRepositoryImpl.findById(saved.id)

        // then
        assertThat(found).isNotNull()
        assertThat(found!!.id).isEqualTo(saved.id)
        assertThat(found.customerId).isEqualTo(saved.customerId)
    }

    @Test
    fun `존재하지 않는 ID 조회 시 null을 반환해야 한다`() {
        // when
        val result = orderRepositoryImpl.findById(OrderId(UUID.randomUUID()))

        // then
        assertThat(result).isNull()
    }
}
```

### 규칙
- `@DataJpaTest` — JPA 레이어만 로드 (H2 인메모리 DB)
- `RepositoryImpl`을 직접 인스턴스화하여 `JpaRepository` 주입
- 도메인 객체 ↔ JPA Entity 변환 정확성 검증
- CRUD 시나리오 전체 커버

---

## 테스트 파일 위치

```
src/test/kotlin/com/example/demo/
├── domain/
│   └── model/          # Domain 단위 테스트
├── application/
│   └── service/        # Service 단위 테스트
├── presentation/
│   └── {domain}/       # Controller 슬라이스 테스트
├── infrastructure/
│   └── persistence/
│       └── {domain}/   # Repository 슬라이스 테스트
└── fixture/            # 공통 Fixture 오브젝트
```
