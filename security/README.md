# Spring Security Demo

A Spring Boot MVC application demonstrating Spring Security with role-based access control (RBAC), form-based authentication, and security best practices.

## Features

- **Role-Based Access Control (RBAC)**: Three roles (EMPLOYEE, MANAGER, ADMIN) with different access levels
- **Form-Based Authentication**: Custom login page with Thymeleaf templates
- **Password Encoding**: BCrypt password hashing
- **Remember-Me Authentication**: Persistent login with cookie-based tokens
- **Session Management**: Single session per user with automatic invalidation
- **Method-Level Security**: `@PreAuthorize` annotation support
- **Security Headers**: Frame options, Content Security Policy
- **Thymeleaf Security Dialect**: Conditional rendering based on roles

## Quick Start

```bash
# Run the application
./gradlew :security:bootRun

# Run tests
./gradlew :security:test
```

Access the application at `http://localhost:8080`

## Demo Users

| Username | Password | Roles              | Access                     |
|----------|----------|--------------------|----------------------------|
| john     | 123      | EMPLOYEE           | Home page only             |
| peter    | 123      | EMPLOYEE, MANAGER  | Home + Leaders pages       |
| frank    | 123      | EMPLOYEE, ADMIN    | Home + Systems pages       |

## Endpoints

| Path           | Access          | Description              |
|----------------|-----------------|--------------------------|
| `/my-login/`   | Public          | Login page               |
| `/`            | EMPLOYEE        | Home page                |
| `/leaders/`    | MANAGER         | Leadership meeting page  |
| `/systems/`    | ADMIN           | System activity logs     |
| `/access-denied` | Authenticated | Access denied error page |

## Security Configuration

### Password Encoding

Uses `BCryptPasswordEncoder` for secure password hashing:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Remember-Me

Token-based remember-me with 24-hour validity:

```java
.rememberMe(remember -> remember
    .key("uniqueAndSecretKey")
    .tokenValiditySeconds(86400)
)
```

### Session Management

Single session per user with expired session redirect:

```java
.sessionManagement(session -> session
    .maximumSessions(1)
    .expiredUrl("/my-login/?expired")
)
```

### Security Headers

```java
.headers(headers -> headers
    .frameOptions(frame -> frame.deny())
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
)
```

### Method-Level Security

Enabled via `@EnableMethodSecurity`:

```java
@PreAuthorize("hasRole('MANAGER')")
public void managerOnlyMethod() { }

@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
public UserProfile getUserProfile(String userId) { }
```

## Project Structure

```
security/
├── src/main/java/com/example/security/
│   ├── MainApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   └── controller/
│       ├── HomeController.java
│       ├── LoginController.java
│       ├── LeadersController.java
│       └── SystemsController.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/
│       ├── home/index.html
│       ├── login/index.html
│       ├── login/access-denied.html
│       ├── leaders/index.html
│       └── systems/index.html
└── src/test/java/
    ├── MainApplicationTests.java
    ├── SecurityTests.java
    └── EncryptionTests.java
```

## Testing

The project includes comprehensive security tests using Spring Security Test:

```java
@WebMvcTest
@Import(SecurityConfig.class)
class SecurityTests {

    @Test
    void homeRequiresAuthentication() { }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanAccessHome() { }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotAccessLeaders() { }
}
```

Run tests:

```bash
./gradlew :security:test
```

## Technologies

- Java 21+
- Spring Boot 4
- Spring Security
- Thymeleaf + Spring Security Dialect
- H2 Database (embedded)
- Lombok
- JUnit 5
