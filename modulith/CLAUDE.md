# Modulith Project

Spring Modulith demo showcasing modular monolith architecture with event-driven communication.

## Quick Start

```bash
# Start MySQL
docker run -d --name demodb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=demodb -p 3306:3306 mysql:8.4

# Run application
./gradlew modulith:bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/modulith

## Architecture

```
modulith/
├── customer/          # Customer registration (no dependencies)
├── order/             # Order management (depends on customer)
├── inventory/         # Stock management (depends on order for events)
└── shared/            # Cross-cutting concerns (API response, exceptions)
```

### Module Dependencies

| Module    | Allowed Dependencies  | Publishes                                                                        | Listens To                                           |
|-----------|-----------------------|----------------------------------------------------------------------------------|------------------------------------------------------|
| customer  | shared::api           | CustomerRegisteredEvent                                                          | -                                                    |
| order     | customer, shared::api | OrderCreatedEvent, OrderConfirmedEvent, OrderCancelledEvent, OrderCompletedEvent | CustomerRegisteredEvent, StockReservationFailedEvent |
| inventory | order, shared::api    | StockReservedEvent, StockReservationFailedEvent                                  | OrderCreatedEvent, OrderCancelledEvent               |

## Key Patterns

### Module Boundaries

Each module declares dependencies in `package-info.java`:

```java
@ApplicationModule(
    displayName = "Order Module",
    allowedDependencies = {"customer", "shared::api"}
)
package com.example.modulith.order;
```

### Facade Pattern

Modules expose public APIs via Facade classes:

```java
@Component
public class CustomerFacade {
    public boolean customerExists(Long customerId) { ... }
}
```

### Event-Driven Communication

Cross-module communication uses Spring events:

```java
// Publishing (OrderService)
eventPublisher.publishEvent(new OrderCreatedEvent(orderId, customerId, amount, sku, quantity, createdAt));

// Listening (InventoryEventListener)
@ApplicationModuleListener
public void on(OrderCreatedEvent event) { ... }
```

### Order State Machine

```
PENDING → CONFIRMED → COMPLETED
    ↘         ↘
     CANCELLED  CANCELLED
```

## API Endpoints

| Method | Endpoint                              | Description                                   |
|--------|---------------------------------------|-----------------------------------------------|
| POST   | /api/customers                        | Register customer                             |
| GET    | /api/customers/{id}                   | Get customer                                  |
| GET    | /api/customers/{id}/exists            | Check customer exists                         |
| GET    | /api/customers                        | List customers (paginated)                    |
| POST   | /api/orders                           | Create order                                  |
| GET    | /api/orders/{orderId}                 | Get order                                     |
| GET    | /api/orders                           | List orders (paginated, filter by customerId) |
| PATCH  | /api/orders/{orderId}/confirm         | Confirm order (PENDING → CONFIRMED)           |
| PATCH  | /api/orders/{orderId}/cancel          | Cancel order                                  |
| PATCH  | /api/orders/{orderId}/complete        | Complete order (CONFIRMED → COMPLETED)        |
| POST   | /api/inventory/products               | Create product                                |
| GET    | /api/inventory/products               | List products (paginated)                     |
| GET    | /api/inventory/products/{sku}         | Get product by SKU                            |
| PUT    | /api/inventory/products/{sku}         | Update product (name, price)                  |
| POST   | /api/inventory/products/{sku}/restock | Restock product                               |
| GET    | /api/inventory/check/{sku}            | Check stock availability                      |
| POST   | /api/inventory/reserve                | Reserve stock                                 |

## Database

- MySQL 8.4 with Flyway migrations
- Tables: `customers`, `orders`, `products`, `event_publication`
- Event publication table tracks async event completion

## Tech Stack

- Java 21+ with Virtual Threads
- Spring Boot 3.x + Spring Modulith
- JPA/Hibernate, Flyway, Lombok
- springdoc-openapi for Swagger
- spring-modulith-observability for distributed tracing

## Testing

```bash
./gradlew modulith:test
```

Spring Modulith provides module structure verification via `spring-modulith-starter-test`.
`ModulithStructureTests` also generates PlantUML module documentation.

## Configuration

Key settings in `application.yml`:
- Virtual threads enabled
- JPA open-in-view disabled
- Modulith event JDBC persistence enabled
- Events republished on restart (resilience)
- Actuator exposes health, metrics, modulith endpoints
