# Demo GraphQL with Java

## Run project

```bash
./gradlew graphql:clean graphql:bootRun
```

## Testing query

Open browser and access to `http://localhost:8080/graphiql`

## Validation Rules

### Student Validation
- **Name**: Required, 2-100 characters, only letters, spaces, hyphens, and apostrophes
- **Address**: Optional, max 200 characters
- **Date of Birth**: Optional, format YYYY-MM-DD, not in future, age must be 5-100 years

### Vehicle Validation
- **Type**: Required
- **Student Assignment**: When assigning to a student:
  - **BICYCLE**: No age restriction
  - **CAR, TRUCK, VAN, BUS**: Student must be 16+ years old
  - **MOTORCYCLE, SCOOTER**: Student must be 18+ years old
  - Maximum 5 vehicles per student

### Create data

```graphql
mutation {
    createStudent(input: {
        name: "John Doe"
        address: "123 Main Street"
        dateOfBirth: "2000-01-15"
    }) {
        id
        name
        dateOfBirth
    }
}
```

```graphql
# First create an adult student (18+ for car)
mutation {
    createStudent(input: {
        name: "Adult Driver"
        address: "456 Oak Avenue"
        dateOfBirth: "1995-06-20"
    }) {
        id
        name
        dateOfBirth
    }
}
```

```graphql
# Then assign a vehicle using the returned student ID
mutation {
    createVehicle(input: {
        type: CAR
        studentId: "81dc2069-3146-42b8-b86e-71812d25e9f2"
    }) {
        id
        type
        student {
            name
        }
    }
}
```

### Bulk create data

```graphql
mutation {
    createStudents(inputs: [
        { name: "Alice Johnson", address: "123 Elm Street", dateOfBirth: "1998-03-15" },
        { name: "Bob Smith", address: "456 Maple Avenue", dateOfBirth: "1999-07-22" },
        { name: "Charlie Brown", address: "789 Oak Drive", dateOfBirth: "2000-11-08" }
    ]) {
        id
        name
        dateOfBirth
    }
}
```

```graphql
# Bulk create vehicles for students who meet age requirements
# Note: Replace with actual student IDs from previous queries
mutation {
    createVehicles(inputs: [
        { type: CAR, studentId: "ADULT_STUDENT_ID_1" }
        { type: MOTORCYCLE, studentId: "ADULT_STUDENT_ID_2" }
        { type: BICYCLE, studentId: "ANY_STUDENT_ID" }
    ]) {
        id
        type
        student {
            name
            dateOfBirth
        }
    }
}
```

### Partial update (patch-like behavior)

Only provided fields will be updated. Throws RESOURCE_NOT_FOUND error if entity not found.

```graphql
mutation {
    updateStudent(input: {
        id: "EXISTING_STUDENT_ID"
        name: "John Updated Smith"
        address: "999 New Address Lane"
    }) {
        id
        name
        address
        dateOfBirth
        updatedAt
    }
}
```

```graphql
mutation {
    updateVehicle(input: {
        id: "EXISTING_VEHICLE_ID"
        type: TRUCK
    }) {
        id
        type
        student {
            id
            name
        }
        updatedAt
    }
}
```

### Upsert (create or update)

Creates a new entity if id is null or not found. Updates existing entity if found.

```graphql
# Create new student (no id provided)
mutation {
    upsertStudent(input: {
        name: "Mary O'Brien-Smith"
        address: "321 Pine Street"
        dateOfBirth: "1997-04-10"
    }) {
        id
        name
        address
        dateOfBirth
        createdAt
        updatedAt
    }
}
```

```graphql
# Update existing student or create if not found
mutation {
    upsertStudent(input: {
        id: "OPTIONAL_EXISTING_ID"
        name: "Updated Mary Smith"
        address: "654 Cedar Lane"
        dateOfBirth: "1997-04-10"
    }) {
        id
        name
        address
        dateOfBirth
        updatedAt
    }
}
```

```graphql
# Upsert vehicle (student must meet age requirements)
mutation {
    upsertVehicle(input: {
        id: "OPTIONAL_EXISTING_ID"
        type: VAN
        studentId: "ADULT_STUDENT_ID"
    }) {
        id
        type
        student {
            id
            name
            dateOfBirth
        }
        updatedAt
    }
}
```

### Query data

```graphql
{
    students(limit: 10) {
        id
        name
        address
        dateOfBirth
    }
}
```

```graphql
{
    student(id: "EXISTING_STUDENT_ID") {
        id
        name
        address
        dateOfBirth
        createdAt
        updatedAt
    }
}
```

```graphql
{
    vehicles(limit: 10) {
        id
        type
        student {
            id
            name
            address
            dateOfBirth
        }
    }
}
```

> **Note:** The `students` and `vehicles` queries are deprecated. Use `studentsPage` and `vehiclesPage` instead. GraphiQL will show deprecation warnings when using these queries.

### Offset-based Pagination with Filter and Sort

Query students with pagination, filtering by name, and sorting by creation date:

```graphql
{
    studentsPage(
        page: { page: 0, size: 10 }
        filter: { name: { contains: "John" } }
        sort: { field: CREATED_AT, direction: DESC }
    ) {
        content {
            id
            name
            address
            dateOfBirth
            createdAt
        }
        pageInfo {
            totalElements
            totalPages
            currentPage
            pageSize
            hasNext
            hasPrevious
        }
    }
}
```

Filter students by address starting with a prefix:

```graphql
{
    studentsPage(
        filter: { address: { startsWith: "123" } }
        sort: { field: NAME, direction: ASC }
    ) {
        content {
            id
            name
            address
            dateOfBirth
        }
        pageInfo {
            totalElements
            hasNext
        }
    }
}
```

### Cursor-based Pagination (Relay Connection Pattern)

First page of students using cursor pagination:

```graphql
{
    studentsConnection(
        connection: { first: 5 }
        sort: { field: CREATED_AT, direction: DESC }
    ) {
        edges {
            node {
                id
                name
                address
            }
            cursor
        }
        pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
        }
        totalCount
    }
}
```

Next page using cursor (use `endCursor` from previous response):

```graphql
{
    studentsConnection(
        connection: { first: 5, after: "CURSOR_FROM_PREVIOUS_RESPONSE" }
    ) {
        edges {
            node {
                id
                name
                dateOfBirth
            }
            cursor
        }
        pageInfo {
            hasNextPage
            endCursor
        }
        totalCount
    }
}
```

### Vehicle Pagination with Enum Filter

Query vehicles filtering by type enum:

```graphql
{
    vehiclesPage(
        page: { page: 0, size: 10 }
        filter: { type: { in: [CAR, MOTORCYCLE] } }
        sort: { field: TYPE, direction: ASC }
    ) {
        content {
            id
            type
            student {
                id
                name
            }
            createdAt
        }
        pageInfo {
            totalElements
            hasNext
        }
    }
}
```

Filter vehicles by exact type:

```graphql
{
    vehiclesPage(
        filter: { type: { eq: CAR } }
    ) {
        content {
            id
            type
            student {
                name
            }
        }
        pageInfo {
            totalElements
        }
    }
}
```

Filter vehicles by student ID:

```graphql
{
    vehiclesPage(
        filter: { studentId: { eq: "EXISTING_STUDENT_ID" } }
    ) {
        content {
            id
            type
            student {
                name
                dateOfBirth
            }
        }
        pageInfo {
            totalElements
        }
    }
}
```

### Vehicle Cursor-based Pagination

```graphql
{
    vehiclesConnection(
        connection: { first: 10 }
        filter: { type: { in: [CAR, TRUCK, VAN] } }
        sort: { field: CREATED_AT, direction: DESC }
    ) {
        edges {
            node {
                id
                type
                student {
                    name
                }
            }
            cursor
        }
        pageInfo {
            hasNextPage
            endCursor
        }
        totalCount
    }
}
```

### API Version Info

Check the current API version and deprecated features:

```graphql
{
    apiVersion {
        version
        deprecatedFeatures
        supportedUntil
    }
}
```

## Available VehicleType Enum Values

- `CAR`
- `MOTORCYCLE`
- `BICYCLE`
- `TRUCK`
- `BUS`
- `VAN`
- `SCOOTER`

## Filter Options

### StringFilter
- `eq` - Exact match
- `contains` - Contains substring (case-insensitive)
- `startsWith` - Starts with prefix (case-insensitive)
- `endsWith` - Ends with suffix (case-insensitive)
- `in` - Match any value in list

### DateTimeFilter
- `eq` - Exact match
- `gt` - Greater than
- `gte` - Greater than or equal
- `lt` - Less than
- `lte` - Less than or equal
- `between` - Between range (start, end)

### VehicleTypeFilter
- `eq` - Exact enum match
- `in` - Match any enum value in list

### UUIDFilter
- `eq` - Exact UUID match
- `in` - Match any UUID in list

## Sort Fields

### StudentSortField
- `NAME`
- `ADDRESS`
- `DATE_OF_BIRTH`
- `CREATED_AT`
- `UPDATED_AT`

### VehicleSortField
- `TYPE`
- `CREATED_AT`
- `UPDATED_AT`

### SortDirection
- `ASC` - Ascending
- `DESC` - Descending

## Error Handling & Validation Examples

The API implements comprehensive error handling with detailed error messages. See [ERROR_HANDLING_README.md](ERROR_HANDLING_README.md) for complete documentation.

### Validation Error Example

```graphql
# Invalid: Name too short (must be at least 2 characters)
mutation {
    createStudent(input: {
        name: "J"
        dateOfBirth: "2000-01-01"
    }) {
        id
    }
}
```

**Response:**
```json
{
  "errors": [{
    "message": "Student creation validation failed",
    "extensions": {
      "errorCode": "VALIDATION_ERROR",
      "statusCode": 400,
      "fieldErrors": {
        "name": "Name must be at least 2 characters"
      }
    }
  }]
}
```

### Business Rule Violation Example

```graphql
# Invalid: Student too young for a car (must be 16+)
mutation {
    createStudent(input: {
        name: "Young Student"
        dateOfBirth: "2018-01-01"
    }) {
        id
    }
}
```

```graphql
# Then try to assign a car - will fail
mutation {
    createVehicle(input: {
        type: CAR
        studentId: "YOUNG_STUDENT_ID"
    }) {
        id
    }
}
```

**Response:**
```json
{
  "errors": [{
    "message": "Student 'Young Student' (age 6) is too young to have a car. Minimum age: 16",
    "extensions": {
      "errorCode": "VEHICLE_ASSIGNMENT_ERROR",
      "statusCode": 422
    }
  }]
}
```

### Resource Not Found Example

```graphql
query {
    student(id: "00000000-0000-0000-0000-000000000000") {
        id
        name
    }
}
```

**Response:**
```json
{
  "errors": [{
    "message": "Student with id '00000000-0000-0000-0000-000000000000' not found",
    "extensions": {
      "errorCode": "RESOURCE_NOT_FOUND",
      "statusCode": 404
    }
  }]
}
```

### Valid Examples with Age Restrictions

```graphql
# Valid: 17-year-old can have a car (16+ required)
mutation {
    createStudent(input: {
        name: "Teen Driver"
        dateOfBirth: "2007-01-01"
    }) {
        id
        name
        dateOfBirth
    }
}
```

```graphql
mutation {
    createVehicle(input: {
        type: CAR
        studentId: "TEEN_STUDENT_ID"
    }) {
        id
        type
    }
}
```

```graphql
# Valid: Young child can have a bicycle (no age restriction)
mutation {
    createStudent(input: {
        name: "Young Cyclist"
        dateOfBirth: "2018-01-01"
    }) {
        id
        name
    }
}
```

```graphql
mutation {
    createVehicle(input: {
        type: BICYCLE
        studentId: "CHILD_STUDENT_ID"
    }) {
        id
        type
    }
}
```

```graphql
# Valid: 18+ can have motorcycle
mutation {
    createStudent(input: {
        name: "Adult Rider"
        dateOfBirth: "1995-01-01"
    }) {
        id
        name
    }
}
```

```graphql
mutation {
    createVehicle(input: {
        type: MOTORCYCLE
        studentId: "ADULT_STUDENT_ID"
    }) {
        id
        type
    }
}
```
