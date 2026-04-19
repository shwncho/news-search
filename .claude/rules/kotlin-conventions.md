# Kotlin 코드 컨벤션

이 파일은 프로젝트 전반의 Kotlin 코드 작성 규칙을 정의한다.
`implement.md`와 `reviewer.md`가 이 규칙을 참조한다.

---

## 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스·인터페이스 | PascalCase | `OrderService`, `UserRepository` |
| 함수·변수 | camelCase | `findById`, `userName` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | 소문자·점 구분 | `com.example.demo.domain.model` |
| 테스트 함수 | 백틱 허용 | `` `주문 생성 시 저장되어야 한다`() `` |

---

## 불변성

```kotlin
// 선호: val 기본
val name: String = "Alice"

// 회피: var는 꼭 필요한 경우만 (loop counter, builder pattern 등)
var retryCount = 0
```

- 클래스 프로퍼티는 `val` 우선
- `MutableList`·`MutableMap` 등 mutable 컬렉션은 클래스 내부에서만 사용하고 외부에는 불변 타입(`List`, `Map`)으로 공개

---

## Null 안전성

```kotlin
// 선호: 엘비스 연산자
val name = user?.profile?.name ?: "Unknown"

// 선호: 안전 호출 + 예외
fun requireOrder(id: OrderId): Order =
    orderRepository.findById(id) ?: throw OrderNotFoundException(id)
```

- `!!` 연산자 사용 금지 — 런타임 NPE 위험, 도메인 예외로 대체
- `lateinit var`는 DI 또는 테스트 setUp에서만 허용
- nullable 타입(`?`)은 의미상 null이 유효한 값인 경우에만 사용

---

## 타입 시스템 활용

### Value Object

```kotlin
// 식별자: JvmInline value class
@JvmInline
value class OrderId(val value: UUID) {
    companion object {
        fun generate() = OrderId(UUID.randomUUID())
        fun of(value: String) = OrderId(UUID.fromString(value))
    }
}

// 복합 VO: data class
data class Money(val amount: BigDecimal, val currency: Currency) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "통화가 다릅니다" }
        return Money(amount + other.amount, currency)
    }
}
```

### sealed class / sealed interface

```kotlin
// 유한한 상태·결과 표현에 사용
sealed interface PaymentResult {
    data class Success(val transactionId: String) : PaymentResult
    data class Failure(val reason: String) : PaymentResult
}
```

### when 표현식

```kotlin
// exhaustive하게 처리 (else 브랜치 최소화)
val label = when (status) {
    OrderStatus.PENDING    -> "대기중"
    OrderStatus.CONFIRMED  -> "확인됨"
    OrderStatus.CANCELLED  -> "취소됨"
}
```

---

## 함수형 스타일

```kotlin
// 컬렉션 변환
val responses = orders.map { OrderResponse.from(it) }
val activeOrders = orders.filter { it.status == OrderStatus.CONFIRMED }

// 스코프 함수
val order = orderRepository.findById(id)?.also {
    log.debug("Order found: ${it.id}")
} ?: throw OrderNotFoundException(id)

// let: nullable 처리
user?.let { sendWelcomeEmail(it) }

// apply: 객체 초기화
val config = AppConfig().apply {
    timeout = Duration.ofSeconds(30)
    retries = 3
}
```

스코프 함수 선택 기준:
- `let`: nullable 체인, 결과 변환
- `run`: 객체 컨텍스트에서 계산 후 결과 반환
- `apply`: 객체 설정 후 동일 객체 반환
- `also`: 부수 효과(로깅 등), 동일 객체 반환
- `with`: non-null 수신 객체에 여러 연산

---

## 확장 함수

```kotlin
// 도메인 의미가 있는 변환은 확장 함수로
fun String.toOrderId(): OrderId = OrderId(UUID.fromString(this))
fun UUID.toOrderId(): OrderId = OrderId(this)
```

- 확장 함수는 대상 타입과 관련된 파일 또는 별도 `Extensions.kt`에 위치
- 전역 유틸성 확장 함수 남발 금지

---

## 클래스 설계

```kotlin
// 생성자 파라미터가 많으면 named argument 사용
val order = Order.create(
    customerId = customerId,
    shippingAddress = address,
)

// data class: DTO, Value Object에 사용
// 도메인 Aggregate·Entity는 일반 class (copy() 노출 방지)
```

---

## ktlint 규칙

- 들여쓰기: 4칸 (space)
- 최대 줄 길이: 120자
- 후행 쉼표(trailing comma): 허용
- import 정렬: 알파벳 순, wildcard import 금지
- 중괄호: 같은 줄에 시작

```bash
./gradlew ktlintCheck   # 검사
./gradlew ktlintFormat  # 자동 수정
```
