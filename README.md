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
- ✅ MDC Logging with `requestId` and `userId`
- ✅ Swagger OpenAPI documentation
- ✅ Resilience4j Retry + Circuit Breaker when calling Product Service
- ✅ Prometheus metrics via Spring Actuator
- ✅ Role-based admin control for status updates and audit logs

---

## 🏗️ Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security (OAuth2 JWT)
- Spring Data JPA (MySQL)
- Spring Kafka (Producer + Consumer)
- Spring Retry & Resilience4j
- Micrometer + Prometheus + Actuator
- Logback + SLF4J + MDC
- Swagger (springdoc-openapi)
- Eureka Client (for service discovery)
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

All endpoints are secured with OAuth2 (JWT bearer). Admin-only endpoints require `ROLE_ADMIN` authority.

---

## 🗃️ Database Tables

- `orders`
- `order_item`
- `cart`
- `cart_item`
- `order_audit_log`
- `inventory_rollback_queue`

---

## 🚚 Kafka Topics

- `order_placed` – emitted on order placement
- `order_cancelled` – emitted on cancellation
- `payment_failed` – consumed to cancel and rollback order

---

## 🔁 Retry & Resilience

- `ProductClient` uses **Resilience4j Retry** + **CircuitBreaker** with fallback
- **RollbackRetryScheduler** retries inventory rollback if initial attempt fails (e.g., service down)
- **OrderExpiryScheduler** auto-cancels unpaid orders after TTL

---

## 📈 Metrics (Prometheus)

- `orders.placed.total`
- `orders.cancelled.total`
- `orders.expired.total`
- `orders.status.updated{status}`
- `rollback.retry.success`
- `rollback.retry.failure`

---

## 🧾 MDC Logs

Every log line includes:
```
[requestId] [userId] LEVEL ClassName - Message
```

---

## 🚀 Local Setup

```bash
# Start MySQL, Kafka, Zookeeper, Eureka Server, Auth Service
# Set environment or application.properties

cd order-service
./mvnw clean install
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

Or use Docker/Docker Compose if configured.

---

## 🧠 Future Enhancements

- 🔐 Enforce JWT scopes/claims in fine-grained manner (e.g., `SCOPE_ORDER_WRITE`)
- 🧪 Add full integration test suite using Testcontainers
- 🧾 Invoice generation and email attachment
- 📩 Retry Kafka publishing via outbox pattern
- 💼 Switch to Flyway for DB migrations (currently using `ddl-auto=update`)
- 📬 Email notifications for status changes (e.g., shipped/delivered)
- 📦 Automatic order packing/delivery simulation via Kafka events

---

## 📂 Author
Built by Aayush Kumar for a production-grade backend microservices system.