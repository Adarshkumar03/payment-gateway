# Payment Gateway API

A small Spring Boot 3 service that creates accounts and transfers money between them, safely:
atomic transfers, idempotent retries, and consistent error responses.

## Run it

Requirements: Java 17+, Maven.

```bash
mvn spring-boot:run
```

- API: http://localhost:8080
- H2 console: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:paymentdb`, user `sa`, password `password`)
- Four sample accounts (ids 1-4) are loaded from `data.sql` on every start.

Run the tests: `mvn test`

## Endpoints

| Method | Path             | Description                                  |
|--------|------------------|----------------------------------------------|
| POST   | `/accounts`      | Create an account                            |
| GET    | `/accounts`      | List accounts (id + holder name)             |
| GET    | `/accounts/{id}` | Get one account with its balance             |
| POST   | `/payments`      | Transfer money (requires `Idempotency-Key`)  |
| GET    | `/payments/{id}` | Get a payment and its status                 |

## Sample requests

```bash
# create an account
curl -X POST localhost:8080/accounts -H "Content-Type: application/json" \
  -d '{"holderName":"Meera","balance":1500.00}'

# fetch it
curl localhost:8080/accounts/5

# transfer 250.00 from account 1 to account 2
curl -X POST localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-1001" \
  -d '{"fromAccountId":1,"toAccountId":2,"amount":250.00}'

# send the SAME request again: same response, balances change only once
curl -X POST localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-1001" \
  -d '{"fromAccountId":1,"toAccountId":2,"amount":250.00}'

# payment status
curl localhost:8080/payments/1
```

Errors share one JSON shape:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "Insufficient funds in account 1", "details": [] }
```

| Situation                                   | Status |
|---------------------------------------------|--------|
| Invalid body, missing header, no funds, same account | 400 |
| Account / payment not found                 | 404    |
| Key reused with a different body, concurrent duplicate, concurrent account update | 409 |

## Design decisions

**BigDecimal for money.** `double`/`float` cannot represent values like 0.10 exactly, so rounding
errors creep in. `BigDecimal` is exact. Columns are `DECIMAL(19,2)`, amounts are limited to 2
decimal places, and comparisons use `compareTo` (not `equals`, which also compares scale).

**@Transactional on the transfer.** The payment record, the debit and the credit are one unit of
work. If anything throws (a `RuntimeException`), Spring rolls the whole transaction back, so
money is never debited without being credited. The unit tests check that failed transfers leave
balances untouched and save no payment. The transfer method lives in a Spring bean called from
the controller, so the transaction proxy applies (no self-invocation pitfall).

**Idempotency.** `POST /payments` requires an `Idempotency-Key` header.
1. If a payment with that key exists, return it and move no money. (If the body differs, return 409.)
2. Otherwise the payment row is inserted and flushed *before* any balance changes. The `payment`
   table has a **unique constraint** on the key, so if two identical requests arrive at the same
   time, the database lets only one through; the loser throws, rolls back, and gets a 409 telling
   the client to retry (the retry then hits step 1 and gets the original result).

**Optimistic locking.** `Account` has a `@Version` column. If two transfers modify the same account
concurrently, the second commit fails instead of silently overwriting the first (returned as 409).

**DTOs, not entities,** are used at the API boundary, with a service layer between controller and
repository, and a single `@RestControllerAdvice` for error handling.

## What I'd add next

- Pagination on `GET /accounts` (and a payments list)
- Security: JWT / OAuth2, and checking the caller owns the sending account
- Postgres instead of H2, with Flyway migrations
- Locking strategy: pessimistic locks (`SELECT ... FOR UPDATE`) in a consistent id order, or automatic
  retry on optimistic-lock failures; currently a conflict returns 409 to the client
- A fuller status lifecycle (`PENDING -> COMPLETED / FAILED`), recording failed attempts, and refunds
- Expiring idempotency keys, and a MockMvc / integration test suite including a concurrency test