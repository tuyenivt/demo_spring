# GraphQL Subproject

Spring Boot GraphQL API for Student-Vehicle management system with advanced pagination, filtering, sorting, and versioning.

## Quick Start

```bash
cd graphql
../gradlew bootRun
```

- GraphiQL UI: http://localhost:8080/graphiql
- GraphQL Endpoint: http://localhost:8080/graphql

## Project Structure

```
src/main/java/com/example/graphql/
├── config/           # GraphQL configuration (scalars)
├── controller/       # GraphQL resolvers (@QueryMapping, @MutationMapping)
├── service/          # Business logic with validation
├── repository/       # JPA repositories with Specification support
├── entity/           # JPA entities (Student, Vehicle)
├── dto/              # Input types, filters, pagination, sorting
├── enums/            # VehicleType enum
├── exception/        # Custom exceptions and error handler
├── validation/       # Business rule validators
├── specification/    # JPA Specification for dynamic filtering
└── util/             # Cursor encoding, sort utilities

src/main/resources/graphql/
├── root.graphqls         # Base Query/Mutation types
├── scalars.graphqls      # UUID, DateTime scalars
├── pagination.graphqls   # PageInput, filters, connection types
├── studentql.graphqls    # Student schema
├── vehicleql.graphqls    # Vehicle schema
└── versioning.graphqls   # V1 deprecated types
```

## Key Technologies

- **Spring Boot 3.x** with Spring GraphQL
- **H2 Database** (embedded, in-memory)
- **JPA/Hibernate** with Specification pattern
- **graphql-java-extended-scalars** for UUID, DateTime
- **Virtual Threads** enabled

## Domain Model

### Student
- id (UUID), name, address, dateOfBirth
- One-to-Many relationship with Vehicle
- Timestamps: createdAt, updatedAt

### Vehicle
- id (UUID), type (enum: CAR, MOTORCYCLE, BICYCLE, TRUCK, BUS, VAN, SCOOTER)
- Many-to-One relationship with Student (optional)
- Timestamps: createdAt, updatedAt

## GraphQL Operations

### Queries

| Query                                          | Description                     |
|------------------------------------------------|---------------------------------|
| `student(id)`                                  | Get student by ID               |
| `studentsPage(page, filter, sort)`             | Offset-based pagination         |
| `studentsConnection(connection, filter, sort)` | Cursor-based (Relay) pagination |
| `vehiclesPage(page, filter, sort)`             | Offset-based pagination         |
| `vehiclesConnection(connection, filter, sort)` | Cursor-based pagination         |
| `apiVersion`                                   | API version info                |

### Mutations

| Mutation                 | Description           |
|--------------------------|-----------------------|
| `createStudent(input)`   | Create single student |
| `createStudents(inputs)` | Bulk create students  |
| `updateStudent(input)`   | Partial update        |
| `upsertStudent(input)`   | Create or update      |
| `createVehicle(input)`   | Create vehicle        |
| `createVehicles(inputs)` | Bulk create vehicles  |
| `updateVehicle(input)`   | Partial update        |
| `upsertVehicle(input)`   | Create or update      |

## Filtering

### StringFilter
```graphql
{ name: { contains: "John" } }
{ name: { startsWith: "J", endsWith: "n" } }
{ name: { in: ["John", "Jane"] } }
```

### DateTimeFilter
```graphql
{ createdAt: { gt: "2024-01-01T00:00:00Z" } }
{ createdAt: { between: { start: "2024-01-01", end: "2024-12-31" } } }
```

### VehicleTypeFilter
```graphql
{ type: { eq: CAR } }
{ type: { in: [CAR, MOTORCYCLE] } }
```

## Pagination Examples

### Offset-based
```graphql
query {
  studentsPage(
    page: { page: 0, size: 10 }
    filter: { name: { contains: "John" } }
    sort: { field: NAME, direction: ASC }
  ) {
    content { id name }
    pageInfo { totalElements hasNext }
  }
}
```

### Cursor-based (Relay)
```graphql
query {
  studentsConnection(
    connection: { first: 10, after: "cursor..." }
  ) {
    edges { cursor node { id name } }
    pageInfo { hasNextPage endCursor }
  }
}
```

## Business Rules

### Student Validation
- Name: 2-100 characters, letters/spaces/hyphens/apostrophes
- Address: max 200 characters
- DateOfBirth: not future, age 5-100 years

### Vehicle Validation
- Max 5 vehicles per student
- Age restrictions by vehicle type:
  - CAR, TRUCK, VAN, BUS: 16+
  - MOTORCYCLE, SCOOTER: 18+
  - BICYCLE: no restriction

## Error Handling

Custom error codes in `ErrorCode` enum:
- `VALIDATION_ERROR` - Input validation failed
- `RESOURCE_NOT_FOUND` - Entity not found
- `TECHNICAL_ERROR` - Internal server error

Errors include field-level details for validation failures.

## Configuration

Key settings in `application.yml`:
```yaml
server.port: 8080
spring.graphql.graphiql.enabled: true
spring.threads.virtual.enabled: true
```

## Testing

```bash
../gradlew test
```

GraphQL test support available via `spring-graphql-test`.
