# modulith

## Overview

- A modulith Spring demo project, showcasing clean module boundaries and event-driven communication.

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

- Handles order creation and lifecycle
- Depends on: Customer module (to validate customers exist)
- Listens to `CustomerRegisteredEvent` (could send welcome offers)
- Publishes `OrderCreatedEvent`

### Inventory Module

- Manages product catalog and stock levels
- Handles stock reservations
- Listens to `OrderCreatedEvent` (could reserve stock automatically)
- Zero dependencies on other business modules

## Communication Patterns

1. Synchronous Communication (Direct API Calls)
    Used when immediate response is needed:
    ```bash
    // Order module calling Customer module
    if (!customerFacade.customerExists(command.customerId())) {
        throw new IllegalArgumentException("Customer does not exist");
    }
    ```
2. Asynchronous Communication (Events)
    Used for cross-cutting concerns and eventual consistency:
    ```bash
    // Customer module publishes event
    eventPublisher.publishEvent(new CustomerRegisteredEvent(...));
    
    // Customer module publishes event
    eventPublisher.publishEvent(new CustomerRegisteredEvent(...));
    
    // Order module listens
    @ApplicationModuleListener
    void on(CustomerRegisteredEvent event) {
    // React to customer registration
    }
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
    "totalAmount": 99.99
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
curl http://localhost:8080/api/inventory/check/LAPTOP-001?quantity=5
```
