# RabbitMQ Subproject

## Overview

Spring Boot application demonstrating RabbitMQ messaging with RPC (Request-Reply) pattern using Topic Exchange.

## Project Structure

```
rabbitmq/
├── build.gradle
└── src/main/java/com/coloza/sample/rabbitmq/
    ├── Application.java       # Main config, queue/exchange/binding beans
    ├── Runner.java            # Message producer (CommandLineRunner)
    ├── Receiver.java          # Message consumer
    ├── SendObjectMessage.java # Request DTO
    └── RelyObjectMessage.java # Reply DTO
```

## Key Components

| Component | Purpose |
|-----------|---------|
| `Application` | Configures Queue, TopicExchange, Binding, MessageListenerAdapter |
| `Runner` | Sends 5 messages using `RabbitTemplate.convertSendAndReceive()` |
| `Receiver` | Receives messages, returns reply objects |
| `SendObjectMessage` | Request payload (id, sendMessage) |
| `RelyObjectMessage` | Reply payload (id, relyMessage) |

## RabbitMQ Configuration

- **Exchange**: `sample-spring-messaging-rabbitmq-exchange` (Topic)
- **Queue**: `sample-spring-messaging-rabbitmq` (non-durable)
- **Routing Key**: `foo.bar.#` (wildcard pattern)
- **Pattern**: RPC (synchronous request-reply)

## Message Flow

1. Runner sends `SendObjectMessage` to exchange with routing key `foo.bar.baz`
2. Exchange routes to queue (matches `foo.bar.#` pattern)
3. Receiver consumes and returns `RelyObjectMessage`
4. Runner receives reply via RabbitMQ RPC mechanism

## Dependencies

- `spring-boot-starter-amqp` - Spring AMQP for RabbitMQ
- `lombok` - Boilerplate reduction

## Running

Requires RabbitMQ server running on default port (5672).

```bash
./gradlew :rabbitmq:bootRun
```

## Common Commands

```bash
# Build
./gradlew :rabbitmq:build

# Test
./gradlew :rabbitmq:test

# Run
./gradlew :rabbitmq:bootRun
```
