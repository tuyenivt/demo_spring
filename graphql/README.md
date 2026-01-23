# Demo GraphQL with Java

## Run project

```shell
./gradlew graphql:clean graphql:bootRun
```

## Testing query

Open browser and access to `http://localhost:8080/graphiql`

### Create data

```graphql
mutation {
    createStudent(input: { name: "NAME_1", address: "ADDRESS_1", dateOfBirth: "2000-01-01" }) {
        id
    }
}
```

```graphql
mutation {
    createVehicle(input: { type: CAR, studentId: "81dc2069-3146-42b8-b86e-71812d25e9f2" }) {
        id
    }
}
```

### Bulk create data

```graphql
mutation {
    createStudents(inputs: [
        { name: "NAME_1", address: "ADDRESS_1", dateOfBirth: "2000-01-01" },
        { name: "NAME_2", address: "ADDRESS_2", dateOfBirth: "2001-02-02" },
        { name: "NAME_3", address: "ADDRESS_3", dateOfBirth: "2002-03-03" }
    ]) {
        id
    }
}
```

```graphql
mutation {
    createVehicles(inputs: [
        { type: CAR, studentId: "ea9137ce-a66d-45cf-8ec6-9b687c2458c3" },
        { type: MOTORCYCLE, studentId: "2a4b64a6-e3f7-4f9a-ad60-181d2d199f48" },
        { type: BICYCLE, studentId: "008468e7-8606-47e3-b650-b6c629d5f292" }
    ]) {
        id
    }
}
```

### Partial update (patch-like behavior)

Only provided fields will be updated. Returns null if entity not found.

```graphql
mutation {
    updateStudent(input: {
        id: "81dc2069-3146-42b8-b86e-71812d25e9f2",
        name: "UPDATED_NAME"
    }) {
        id
        name
        address
        dateOfBirth
    }
}
```

```graphql
mutation {
    updateVehicle(input: {
        id: "81dc2069-3146-42b8-b86e-71812d25e9f2",
        type: TRUCK
    }) {
        id
        type
        student {
            id
            name
        }
    }
}
```

### Upsert (create or update)

Creates a new entity if id is null or not found. Updates existing entity if found.

```graphql
mutation {
    upsertStudent(input: {
        id: "81dc2069-3146-42b8-b86e-71812d25e9f2",
        name: "UPSERTED_NAME",
        address: "UPSERTED_ADDRESS",
        dateOfBirth: "1999-12-31"
    }) {
        id
        name
        address
        dateOfBirth
    }
}
```

```graphql
mutation {
    upsertVehicle(input: {
        id: "81dc2069-3146-42b8-b86e-71812d25e9f2",
        type: VAN,
        studentId: "ea9137ce-a66d-45cf-8ec6-9b687c2458c3"
    }) {
        id
        type
        student {
            id
            name
        }
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
    student(id: "81dc2069-3146-42b8-b86e-71812d25e9f2") {
        id
        name
        address
        dateOfBirth
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
        filter: { name: { contains: "NAME" } }
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
        filter: { address: { startsWith: "ADDRESS" } }
        sort: { field: NAME, direction: ASC }
    ) {
        content {
            id
            name
            address
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
        connection: { first: 5, after: "Y3Vyc29yOjgxZGMyMDY5LTMxNDYtNDJiOC1iODZlLTcxODEyZDI1ZTlmMg==" }
    ) {
        edges {
            node {
                id
                name
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
        filter: { studentId: { eq: "81dc2069-3146-42b8-b86e-71812d25e9f2" } }
    ) {
        content {
            id
            type
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
