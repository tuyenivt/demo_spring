# Spring Modulith Demo

## Overview

A modulith Spring demo project, showcasing clean module boundaries and event-driven communication.

## Architecture Overview

This project implements a modular monolith - a single deployable application organized into well-defined, independent modules that communicate via events and explicit APIs.
Each module can later be extracted into a microservice with minimal refactoring.

### Key Architectural Principles

1. Package-by-Module: Code is organized by business capability, not technical layer
2. Module Boundaries: Each module owns its domain, application logic, and infrastructure
3. Event-Driven Communication: Modules interact via application events (asynchronous, decoupled)
4. Public APIs: Modules expose behavior through Facade classes
5. Encapsulation: Internal implementation details (entities, repositories) are package-private

## Modules

### Customer Module

- Manages customer registration and data
- Publishes `CustomerRegisteredEvent` when customers register
- Exposes `CustomerFacade` for other modules to verify customer existence
- Zero dependencies on other business modules

### Order Module

- Handles order creation and lifecycle with a state machine (PENDING → CONFIRMED → COMPLETED, cancellable from any non-terminal state)
- Depends on: Customer module (to validate customers exist)
- Listens to `CustomerRegisteredEvent` (logs welcome event)
- Listens to `StockReservationFailedEvent` (auto-cancels the order)
- Publishes `OrderCreatedEvent`, `OrderConfirmedEvent`, `OrderCancelledEvent`, `OrderCompletedEvent`

### Inventory Module

- Manages product catalog and stock levels
- Handles stock reservations and restocking
- Listens to `OrderCreatedEvent` (attempts automatic stock reservation)
- Listens to `OrderCancelledEvent` (releases reserved stock)
- Publishes `StockReservedEvent` on success or `StockReservationFailedEvent` on failure
- Zero dependencies on other business modules (communicates only via events)

## Communication Patterns

1. Synchronous Communication (Direct API Calls)
    Used when immediate response is needed:
    ```java
    // Order module calling Customer module
    if (!customerFacade.customerExists(command.customerId())) {
        throw new IllegalArgumentException("Customer does not exist");
    }
    ```

2. Asynchronous Communication (Events)
    Used for cross-cutting concerns and eventual consistency:
    ```java
    // Order module publishes event
    eventPublisher.publishEvent(new OrderCreatedEvent(...));

    // Inventory module listens
    @ApplicationModuleListener
    void on(OrderCreatedEvent event) {
        // Attempt stock reservation; publish StockReservationFailedEvent on failure
    }
    ```

## Event Flow (Happy Path)

```
POST /api/customers
  └─ Publishes CustomerRegisteredEvent
       └─ Order module logs welcome

POST /api/orders {customerId, totalAmount, sku, quantity}
  └─ Validates customer exists (via CustomerFacade)
  └─ Saves Order (status=PENDING)
  └─ Publishes OrderCreatedEvent
       └─ Inventory tries reserveStock(sku, quantity)
          ├─ success → Publishes StockReservedEvent
          └─ failure → Publishes StockReservationFailedEvent
                         └─ Order auto-cancelled → Publishes OrderCancelledEvent
                                                      └─ Inventory releases stock

PATCH /api/orders/{id}/confirm  → PENDING → CONFIRMED → Publishes OrderConfirmedEvent
PATCH /api/orders/{id}/complete → CONFIRMED → COMPLETED → Publishes OrderCompletedEvent
PATCH /api/orders/{id}/cancel   → any state → CANCELLED → Publishes OrderCancelledEvent
```

## Quick Start

### Start MySQL with docker

```bash
docker run -d --name demodb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=demodb -p 3306:3306 mysql:8.4
```

### Run app

```bash
./gradlew modulith:clean modulith:bootRun
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## API Tests

Register a Customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test.user@example.com"
  }'
```

Create an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "totalAmount": 99.99,
    "sku": "LAPTOP-001",
    "quantity": 1
  }'
```

Confirm / Complete an Order

```bash
curl -X PATCH http://localhost:8080/api/orders/1/confirm
curl -X PATCH http://localhost:8080/api/orders/1/complete
```

Create a Product

```bash
curl -X POST http://localhost:8080/api/inventory/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "Laptop Pro",
    "price": 999.99,
    "stockQuantity": 50
  }'
```

Reserve Stock

```bash
curl -X POST http://localhost:8080/api/inventory/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "quantity": 2
  }'
```

Check Stock

```bash
curl "http://localhost:8080/api/inventory/check/LAPTOP-001?quantity=5"
```
