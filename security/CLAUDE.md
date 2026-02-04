# Security Subproject

Spring Boot MVC application demonstrating Spring Security with role-based access control (RBAC).

## Tech Stack

- Java 21+ / Spring Boot 4
- Spring Security (form-based authentication, method-level security)
- Thymeleaf (server-side templates with Spring Security dialect)
- H2 Database (embedded, unused)
- Lombok
- JUnit 5 + Spring Security Test

## Project Structure

```
security/
├── src/main/java/com/example/security/
│   ├── MainApplication.java          # Entry point
│   ├── config/
│   │   └── SecurityConfig.java       # Security configuration
│   └── controller/
│       ├── HomeController.java       # GET / (EMPLOYEE)
│       ├── LoginController.java      # Login/access-denied pages
│       ├── LeadersController.java    # GET /leaders/ (MANAGER)
│       └── SystemsController.java    # GET /systems/ (ADMIN)
├── src/main/resources/
│   ├── application.properties
│   └── templates/                    # Thymeleaf templates
│       ├── home/index.html
│       ├── login/index.html
│       ├── login/access-denied.html
│       ├── leaders/index.html
│       └── systems/index.html
└── src/test/java/
    ├── MainApplicationTests.java     # Context load test
    ├── SecurityTests.java            # Security integration tests
    └── EncryptionTests.java          # Encryption utility demos
```

## Security Configuration

### Features

- **Password Encoding**: BCryptPasswordEncoder
- **Method-Level Security**: @EnableMethodSecurity with @PreAuthorize support
- **Remember-Me**: Cookie-based with 1-day validity
- **Session Management**: Single session per user, auto-invalidation
- **Security Headers**: Frame options deny, CSP default-src 'self'
- **Logout**: Session invalidation, JSESSIONID cookie deletion

### Users (In-Memory)

| Username | Password | Roles              |
|----------|----------|--------------------|
| john     | 123      | EMPLOYEE           |
| peter    | 123      | EMPLOYEE, MANAGER  |
| frank    | 123      | EMPLOYEE, ADMIN    |

### Authorization Rules

| Path           | Required Role |
|----------------|---------------|
| `/`            | EMPLOYEE      |
| `/leaders/**`  | MANAGER       |
| `/systems/**`  | ADMIN         |
| Other          | Authenticated |

### Login Configuration

- Login Page: `/my-login/`
- Auth Endpoint: `/my-authenticate`
- Access Denied: `/access-denied`
- Logout Success: `/my-login/?logout`
- Session Expired: `/my-login/?expired`

## Build & Run

```bash
# From project root
./gradlew :security:bootRun

# Run tests
./gradlew :security:test
```

## Key Files

- `SecurityConfig.java` - All security rules, password encoding, session management
- `home/index.html` - Role-based conditional rendering with `sec:authorize`
- `login/index.html` - Login form with remember-me checkbox
- `SecurityTests.java` - Comprehensive security integration tests
- `EncryptionTests.java` - BCrypt and encryption examples
