# 🛒 Order Service - E-commerce Microservice

## 📌 Overview
The **Order Service** is a Spring Boot microservice that handles shopping cart management, order placement, order cancellation, audit logging, status updates, Kafka messaging, and inventory rollback in a production-grade e-commerce architecture.

---

## ⚙️ Features

- ✅ Add/Remove/View items in user-specific cart
- ✅ Place order from cart with shipping details
- ✅ Cancel placed order (before payment)
- ✅ Kafka Integration for:
  - 📦 `order_placed` event
  - ❌ `order_cancelled` event
  - ❗ `payment_failed` rollback
- ✅ Inventory rollback via `prod-cat-service` for failed/cancelled orders
- ✅ Retry mechanism for failed rollback via DB queue
- ✅ Auto-expiry of unpaid orders after configurable TTL (default: 30 mins)
- ✅ MDC Logging with `requestId`, `orderId`, and `userId`
- ✅ Swagger OpenAPI documentation
- ✅ Resilience4j Retry + Circuit Breaker when calling Product Service
- ✅ Prometheus metrics via Spring Actuator
- ✅ Role and scope-based admin control for status updates and audit logs
- ✅ Token introspection with Redis caching
- ✅ SendGrid email notifications
- ✅ Dockerfile + GitHub CI Workflow

---

## 🏗️ Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security (OAuth2 JWT + RBAC)
- Spring Data JPA (MySQL)
- Spring Kafka (Producer + Consumer)
- Spring Retry & Resilience4j
- Micrometer + Prometheus + Actuator
- Logback + SLF4J + MDC
- Swagger (springdoc-openapi)
- Eureka Client (Service Discovery)
- Redis
- Docker-ready

---

## 🧪 APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/order/cart` | View current cart |
| POST   | `/order/cart` | Add item to cart |
| DELETE | `/order/cart/{itemId}` | Remove item |
| POST   | `/order/placeorder` | Place order |
| GET    | `/order/orders` | Paginated order list |
| GET    | `/order/orders/{id}` | Order details |
| PUT    | `/order/orders/{id}/cancel` | Cancel order |
| PUT    | `/order/orders/{id}/status` | Admin: Update status |
| GET    | `/order/orders/{id}/audit-log` | Admin: View audit logs |
| GET    | `/me/orders` | Get all orders for logged-in user |

All endpoints are secured with OAuth2 JWT tokens.

---

## 📬 Kafka Topics

- `order.placed` – published on order placement
- `order.cancelled` – published on cancellation
- `payment.failed` – consumed to trigger rollback
- `order.retry` – optional retry topic

---

## 🛡️ Retry & Resilience

- `ProductClient` uses **Resilience4j Retry** + **CircuitBreaker**
- `RollbackRetryScheduler` retries inventory rollback
- `OrderExpiryScheduler` auto-cancels unpaid orders after TTL
- Kafka publishing uses RetryTemplate

---

## 📈 Metrics (Prometheus)

- `orders.placed.total`
- `orders.cancelled.total`
- `orders.expired.total`
- `orders.status.updated{status}`
- `rollback.retry.success`
- `rollback.retry.failure`

---

## 🔐 Security

- OAuth2 JWT + Token Introspection
- Admin-only endpoints protected by `@AdminOnly`
- Internal service calls secured via role/scope-based claims

---

## 📬 Email Notifications

- Sent via SendGrid for order confirmation/cancellation

---

## 📦 Run Locally

```bash
# Start MySQL, Redis, Kafka, Eureka, Auth
./mvnw clean install
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

---


## 📦 Docker

```bash
docker build -t your-username/order-service .
```

---

## 🧪 Test Coverage

Includes:
- Unit tests (Service Layer)
- Integration tests (Web Layer + Kafka + DB)
- Retry logic and scheduler testing

```bash
mvn test
```

---

## 🔍 Redis Cache

- Redis used for token introspection caching

---

## 📂 Directory Structure

```
order-service/
├── configs/
├── controllers/
├── dtos/
├── models/
├── repositories/
├── services/
├── schedulers/
├── kafka/
├── security/
├── utils/
├── resources/
│   ├── application.properties
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 📚 Swagger UI

Visit [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

---

## 🔮 Future Enhancements

- 🔧 Flyway DB migrations
- 🔧 Order invoicing & attachments
- 🔧 DLQ Kafka monitoring with alerts
- 🔧 Inventory syncing reconciliation
- 🔧 Global exception mapping improvements
- 🔧 Outbox pattern for Kafka reliability
- 🔧 UI-driven order analytics dashboards
- 🔧 Email notifications for status changes (e.g., shipped/delivered)

---

## ✅ Status

**Production-ready and integrated with all services (Auth, Payment, Product Catalog).**