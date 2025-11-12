# Demo GraphQL with Java

## Run project

<code>./gradlew bootRun</code>

## Testing query

<p>Open browser and access to <code>http://localhost:8080/graphql</code></p>
<p>Create new data:</p>
<pre>
mutation {
  createStudent(name: "NAME_1", address: "ADDRESS_1", dateOfBirth: "2000-01-01") {
    id
  }
}
mutation {
  createVehicle(type: "VEHICLE_1", studentId: 1) {
    id
  }
}
</pre>
<p>Query data:</p>
<pre>
{
  students(limit: 10) {
    id
    name
    address
    dateOfBirth
  }
}
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
</pre>
<pre>
{
  student(id: 1) {
    id
    name
    address
    dateOfBirth
  }
}
</pre>
