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
    createVehicle(input: { type: "VEHICLE_1", studentId: "81dc2069-3146-42b8-b86e-71812d25e9f2" }) {
        id
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
