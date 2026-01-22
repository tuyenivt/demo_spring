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
        { type: "VEHICLE_1", studentId: "ea9137ce-a66d-45cf-8ec6-9b687c2458c3" },
        { type: "VEHICLE_2", studentId: "2a4b64a6-e3f7-4f9a-ad60-181d2d199f48" },
        { type: "VEHICLE_3", studentId: "008468e7-8606-47e3-b650-b6c629d5f292" }
    ]) {
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
