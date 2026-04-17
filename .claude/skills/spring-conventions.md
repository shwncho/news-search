# Spring 컴포넌트 설계 컨벤션

이 파일은 Spring Boot 컴포넌트 작성 규칙을 정의한다.
`implement.md`와 `reviewer.md`가 이 규칙을 참조한다.

---

## Application Service

```kotlin
@Service
@Transactional          // 클래스 레벨: 상태 변경 메서드 기본값
class OrderService(
    private val orderRepository: OrderRepository,   // 도메인 Repository 인터페이스
) {
    // 상태 변경: @Transactional 상속
    fun createOrder(command: OrderCommand): OrderResponse {
        val order = Order.create(CustomerId.of(command.customerId))
        return OrderResponse.from(orderRepository.save(order))
    }

    // 조회: readOnly 명시
    @Transactional(readOnly = true)
    fun getOrder(id: String): OrderResponse =
        orderRepository.findById(OrderId.of(id))
            ?.let { OrderResponse.from(it) }
            ?: throw OrderNotFoundException(id)

    @Transactional(readOnly = true)
    fun getAllOrders(): List<OrderResponse> =
        orderRepository.findAll().map { OrderResponse.from(it) }
}
```

### 규칙

- `@Service` 어노테이션만 사용, 별도 인터페이스(UseCase) 생성 금지
- 클래스 레벨 `@Transactional`, 조회 메서드는 `@Transactional(readOnly = true)` 오버라이드
- 의존성은 생성자 주입만 사용 (`private val`)
- 비즈니스 로직은 도메인 객체에 위임, Service는 오케스트레이션만

---

## Controller

```kotlin
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun createOrder(
        @RequestBody @Valid command: OrderCommand,
    ): ResponseEntity<OrderResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(command))

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: String): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(orderService.getOrder(id))

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.getAllOrders())

    @DeleteMapping("/{id}")
    fun deleteOrder(@PathVariable id: String): ResponseEntity<Unit> {
        orderService.deleteOrder(id)
        return ResponseEntity.noContent().build()
    }
}
```

### 규칙

- `@RestController` + `@RequestMapping` 조합
- `application/dto`의 Command/Response DTO를 직접 사용, 별도 Request DTO 생성 금지
- 비즈니스 로직 작성 금지 — Service 호출만
- 입력 검증은 `@Valid` + DTO의 Bean Validation 어노테이션으로
- HTTP 상태 코드: 생성 `201 Created`, 조회 `200 OK`, 삭제 `204 No Content`

---

## Command / Response DTO

```kotlin
// Command: 입력 검증 어노테이션 포함
data class OrderCommand(
    @field:NotBlank val customerId: String,
    @field:NotEmpty val items: List<OrderItemCommand>,
)

data class OrderItemCommand(
    @field:NotBlank val productId: String,
    @field:Min(1) val quantity: Int,
)

// Response: 도메인 객체로부터 변환
data class OrderResponse(
    val id: UUID,
    val customerId: UUID,
    val status: String,
    val itemCount: Int,
    val createdAt: Instant,
) {
    companion object {
        fun from(order: Order) = OrderResponse(
            id = order.id.value,
            customerId = order.customerId.value,
            status = order.status.name,
            itemCount = order.itemCount,
            createdAt = order.createdAt,
        )
    }
}
```

### 규칙

- Command DTO: `@field:` prefix로 Bean Validation 어노테이션 적용
- Response DTO: `companion object { fun from(domain) }` 변환 패턴
- 도메인 객체(Aggregate, Entity)를 DTO 필드로 직접 사용 금지
- 모든 DTO는 `application/dto/` 패키지에 위치

---

## Infrastructure — JPA Entity & Repository

```kotlin
// JPA Entity: Infrastructure 전용
@Entity
@Table(name = "orders")
class OrderJpaEntity(
    @Id
    val id: UUID,

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val LocalDateTime createdAt;

    @LastModifiedDate
    val LocalDateTime updatedAt;
) {
    fun toDomain(): Order = Order.reconstruct(
        id = OrderId(id),
        customerId = CustomerId(customerId),
        status = status,
        createdAt = createdAt,
    )

    companion object {
        fun from(order: Order) = OrderJpaEntity(
            id = order.id.value,
            customerId = order.customerId.value,
            status = order.status,
            createdAt = order.createdAt,
        )
    }
}

// Spring Data JPA Repository
interface OrderJpaRepository : JpaRepository<OrderJpaEntity, UUID>

// Domain Repository 구현체
@Repository
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: Order): Order =
        orderJpaRepository.save(OrderJpaEntity.from(order)).toDomain()

    override fun findById(id: OrderId): Order? =
        orderJpaRepository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<Order> =
        orderJpaRepository.findAll().map { it.toDomain() }

    override fun deleteById(id: OrderId) =
        orderJpaRepository.deleteById(id.value)
}
```

### 규칙

- JPA Entity는 `infrastructure/persistence/{domain}/`에만 위치
- `@Entity` 어노테이션은 Infrastructure 클래스에만
- JPA Entity ↔ Domain 변환(`toDomain()`, `from()`)은 JPA Entity 클래스 내부에서 수행
- Spring Data JPA Repository(`JpaRepository`)는 Infrastructure에만 위치
- Domain Repository 구현체는 `@Repository` 어노테이션

---

## 예외 처리

```kotlin
// Domain Exception: 도메인 레이어 (Spring 의존 없음)
class OrderNotFoundException(id: String) :
    RuntimeException("주문을 찾을 수 없습니다: $id")

// Global Exception Handler: Presentation 레이어 또는 config/
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleNotFound(e: OrderNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "NOT_FOUND", message = e.message ?: ""))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest()
            .body(ErrorResponse(code = "VALIDATION_ERROR", message = message))
    }
}

data class ErrorResponse(val code: String, val message: String)
```

### 규칙

- 도메인 예외 클래스는 `domain/exception/`에, Spring 어노테이션 없이 정의
- `@RestControllerAdvice`로 예외를 HTTP 응답으로 변환하는 책임 분리
- `!!` 연산자 대신 도메인 예외 throw

---

## Spring 설정

```kotlin
// config/ 패키지에 위치
@Configuration
@EnableJpaAuditing
class JpaConfig {
    // JPA 설정
}

@Configuration
@EnableTransactionManagement
class TransactionConfig
```

- 설정 클래스는 `config/` 패키지에 위치
- `@SpringBootApplication`이 있는 메인 클래스에 설정 혼재 금지
