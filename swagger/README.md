# Swagger Demo

Spring Boot demo that generates a Petstore client and now exposes its own REST API facade with Swagger UI.

## What this module does

- Generates `PetApi`, `StoreApi`, and `UserApi` clients from an OpenAPI 3.x spec (`swagger/petstore.yaml`) using `org.openapi.generator`.
- Calls the external Petstore API via Feign + OkHttp.
- Exposes local REST endpoints under `/api/pets` with OpenAPI 3 documentation.

## Run

From repository root:

```bash
./gradlew :swagger:bootRun
```

## API docs

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Exposed endpoints

- `GET /api/pets/{petId}` - get a pet by id
- `GET /api/pets?status=available` - find pets by status (`available|pending|sold`)
- `POST /api/pets` - create a pet

Example create payload:

```json
{
  "name": "Buddy",
  "status": "available"
}
```

## Configuration

Environment variable overrides:

- `PET_STORE_BASE_URL` (default `https://petstore.swagger.io/v2`)
- `PET_STORE_USERNAME` (default `user`)
- `PET_STORE_PASSWORD` (default `pass`)

## Tests

```bash
./gradlew :swagger:test
```
