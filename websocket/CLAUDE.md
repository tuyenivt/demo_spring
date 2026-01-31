# WebSocket Subproject

## Overview

Real-time bidirectional communication demo using Spring Boot WebSocket with STOMP protocol. Features broadcast messaging, private messaging, and system notifications.

## Tech Stack

- Spring Boot WebSocket + STOMP
- SockJS fallback for browser compatibility
- Virtual threads enabled (Java 21+)
- Lombok for boilerplate reduction

## Project Structure

```
websocket/
├── src/main/java/com/example/websocket/
│   ├── DemoWebSocketApplication.java    # Entry point
│   ├── config/
│   │   ├── WebSocketConfig.java         # STOMP/WebSocket configuration
│   │   ├── UserAuthChannelInterceptor.java  # Username extraction from headers
│   │   └── WebSocketEventListener.java  # Connect/disconnect events
│   ├── constant/
│   │   └── WebSocketDestinations.java   # Destination constants
│   ├── controller/
│   │   ├── ChatController.java          # STOMP message handlers
│   │   └── AdminController.java         # REST admin endpoints
│   ├── dto/
│   │   ├── ChatMessage.java             # Input DTO (record)
│   │   ├── ChatResponse.java            # Output DTO (record)
│   │   └── ErrorResponse.java           # Error DTO (record)
│   ├── exception/
│   │   └── WebSocketErrorHandler.java   # Global exception handler
│   └── handler/
│       └── MessageBroadcastHandler.java # Broadcasting logic
├── src/main/resources/
│   ├── application.yml
│   └── static/                          # Test client UI
│       ├── index.html
│       ├── css/styles.css
│       └── js/script.js
└── build.gradle
```

## Key Components

### Configuration

| Class | Purpose |
|-------|---------|
| `WebSocketConfig` | STOMP broker setup, endpoint registration |
| `UserAuthChannelInterceptor` | Extract username from CONNECT headers |
| `WebSocketEventListener` | Broadcast join/leave notifications |

### STOMP Destinations

| Destination | Type | Purpose |
|-------------|------|---------|
| `/ws` | Endpoint | WebSocket connection entry |
| `/app/chat.send` | App | Send broadcast message |
| `/app/chat.private` | App | Send private message |
| `/topic/messages` | Broker | Receive all broadcasts |
| `/topic/notifications` | Broker | System notifications |
| `/user/queue/private` | User | Receive private messages |
| `/user/queue/errors` | User | Receive error responses |

### Controllers

- **ChatController**: `@MessageMapping` handlers for chat operations
  - `handleChatMessage()` - broadcast to all
  - `handlePrivateMessage()` - send to specific user
  - `handleNotificationSubscription()` - welcome message on subscribe

- **AdminController**: REST API for admin operations
  - `POST /api/admin/notify` - broadcast system notification

### DTOs (Java Records)

```java
// Input
record ChatMessage(String username, String content, String targetUsername)

// Output
record ChatResponse(String username, String content, Instant timestamp, String messageType)

// Error
record ErrorResponse(String errorCode, String message, Instant timestamp)
```

## Message Flow

### Broadcast Message
```
Client → /app/chat.send → ChatController → MessageBroadcastHandler → /topic/messages → All clients
```

### Private Message
```
Client → /app/chat.private → ChatController → MessageBroadcastHandler
       → convertAndSendToUser() → /user/{target}/queue/private → Target client
```

### System Notification
```
Event/Admin → MessageBroadcastHandler → /topic/notifications → All clients
```

## Running

```bash
./gradlew websocket:bootRun
```

Access test client at http://localhost:8080

## Configuration

```yaml
server:
  port: 8080

spring:
  threads:
    virtual:
      enabled: true
  jackson:
    serialization:
      write-dates-as-timestamps: false
```

## Important Notes

- **Authentication**: Demo uses header-based username (not secure for production)
- **Message Broker**: In-memory simple broker (single instance only)
- **Validation**: Max message length 1000 chars, no self-messaging
- **XSS Protection**: Frontend escapes HTML in messages

## Testing

Minimal smoke test only. Run with:
```bash
./gradlew websocket:test
```
