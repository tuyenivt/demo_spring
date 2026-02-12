# Versioning Demo

Demonstrates four API versioning strategies in Spring Boot, with cross-cutting features including response envelopes, ETag caching, Micrometer metrics, feature-flag-controlled v1 lifecycle, and OpenAPI grouped docs.

## Quick Start

```bash
./gradlew :versioning:bootRun
```

Server starts on `http://localhost:8080`.

## Prerequisites

- Java 21+

## Versioning Strategies

### 1. URI Path Versioning

Version is embedded in the URL. Spring Data REST uses `basePath=/v2`.

```bash
# V2 (current) — Spring Data REST
curl http://localhost:8080/v2/employees

# V1 (deprecated) — includes Deprecation/Sunset/Link headers
curl -i http://localhost:8080/v1/employees

# V2 with rich DTOs + response envelope
curl http://localhost:8080/api/v2/employees
```

### 2. Custom Header Versioning

Single URI; version is specified via the `Accept-version` header.

```bash
# V2 (explicit)
curl -H "Accept-version: v2" http://localhost:8080/location

# Omitting the header defaults to v2
curl http://localhost:8080/location

# Unsupported version → 400 with structured error
curl -H "Accept-version: v3" http://localhost:8080/location
```

### 3. Media Type Versioning (Content Negotiation)

Version is expressed in the `Accept` header using a vendor MIME type.

```bash
# V1 product
curl -H "Accept: application/vnd.company.v1+json" http://localhost:8080/api/products

# V2 product (adds description, sku)
curl -H "Accept: application/vnd.company.v2+json" http://localhost:8080/api/products

# Unknown media type → 406 with structured error
curl -H "Accept: application/vnd.company.v3+json" http://localhost:8080/api/products
```

### 4. Query Parameter Versioning

Version passed as a query parameter. Default is `v2`.

```bash
# Report V1 (compact: id, title, content)
curl "http://localhost:8080/api/reports?version=1"

# Report V2 (adds author, createdAt, tags) — default
curl http://localhost:8080/api/reports

# Employees via @JsonView: V1 hides title/hireDate/status
curl "http://localhost:8080/api/employees/view?version=1"
curl "http://localhost:8080/api/employees/view?version=2"
```

## Response Envelope

All `/api/**` endpoints wrap their response body in a standard envelope:

```json
{
  "data": { "..." },
  "meta": {
    "apiVersion": "v2",
    "deprecation": null,
    "timestamp": "2026-02-12T00:00:00Z"
  }
}
```

V1 responses include `"deprecation": "2025-12-31"`.

## Version Discovery

```bash
curl http://localhost:8080/api/versions
```

```json
{
  "data": {
    "current": "v2",
    "supported": ["v1", "v2"],
    "deprecated": ["v1"],
    "strategies": ["uri-path", "custom-header", "media-type", "query-parameter"],
    "v1": { "status": "deprecated", "sunset": "2025-12-31", "docs": "/v1/docs" },
    "v2": { "status": "stable", "docs": "/v2/docs" }
  },
  "meta": { "apiVersion": "v2", ... }
}
```

## ETag Caching

`ShallowEtagHeaderFilter` generates ETags for all responses. V1 and V2 produce different ETags for the same resource.

```bash
# First request — note the ETag
curl -i http://localhost:8080/api/v2/employees/1

# Conditional request — 304 Not Modified if unchanged
curl -i -H 'If-None-Match: "<etag-value>"' http://localhost:8080/api/v2/employees/1
```

## Metrics

Per-version request counts tracked by Micrometer and exposed via a custom actuator endpoint.

```bash
curl http://localhost:8080/actuator/api-versions
```

```json
{
  "v1": { "requests": 3, "lastSeen": "2026-02-12T10:00:00Z" },
  "v2": { "requests": 12, "lastSeen": "2026-02-12T10:05:00Z" }
}
```

## Deprecation Headers (V1)

All v1 endpoints respond with:

```
Deprecation: true
Sunset: Sat, 31 Dec 2025 23:59:59 GMT
Link: </v2/employees>; rel="successor-version"
```

## V1 Feature Flag

V1 can be disabled at startup via a Spring profile or property.

```bash
# Disable V1 entirely (EmployeeControllerV1 excluded, /api/versions shows only v2)
./gradlew :versioning:bootRun --args='--spring.profiles.active=v1-disabled'
```

Property file equivalents:
- `application-v1-enabled.properties` → `api.v1.enabled=true`
- `application-v1-disabled.properties` → `api.v1.enabled=false`

## Error Response

Unsupported version requests return a structured error:

```json
{
  "error": "Unsupported API version",
  "requestedVersion": "v3",
  "supportedVersions": ["v1", "v2"],
  "currentVersion": "v2",
  "documentation": "/api/versions"
}
```

| Status | Trigger |
|--------|---------|
| 400 | Unknown value in `Accept-version` header or `?version` query param |
| 406 | Unknown vendor media type in `Accept` header |

## OpenAPI / Swagger UI

SpringDoc groups endpoints by version. Navigate to `http://localhost:8080/swagger-ui.html` and switch between the `v1` and `v2` groups.

## All Endpoints

| Method | Path                   | Description                                      |
|--------|------------------------|--------------------------------------------------|
| GET    | /v2/employees          | List employees (Spring Data REST, paginated HAL) |
| POST   | /v2/employees          | Create employee (Spring Data REST)               |
| GET    | /v1/employees          | List employees V1 (deprecated)                   |
| GET    | /v1/employees/{id}     | Get employee V1 (deprecated)                     |
| GET    | /api/v2/employees      | List employees V2 with envelope                  |
| GET    | /api/v2/employees/{id} | Get employee V2 with ETag                        |
| GET    | /v2/schedule           | Schedule (BasePathAware)                         |
| GET    | /location              | Header-versioned location                        |
| GET    | /v2/location           | Path-versioned location alias                    |
| GET    | /api/products          | Media-type-versioned product                     |
| GET    | /api/reports           | Query-param-versioned report                     |
| GET    | /api/employees/view    | @JsonView field evolution                        |
| GET    | /api/versions          | Version discovery                                |
| GET    | /actuator/health       | Health check                                     |
| GET    | /actuator/api-versions | Per-version request metrics                      |

## Managing Versions

- Group breaking changes into a major version bump (v1 → v2)
- Set a sunset date when releasing a new major version (announce 6+ months ahead)
- Keep old versions independently deployable behind a load balancer
  - Route by URI prefix (`/v1/`, `/v2/`) or custom header value
- Cherry-pick bug fixes from the new version to the old version
- Disable old versions cleanly with `@ConditionalOnProperty` once traffic drops to zero

## Testing

```bash
./gradlew :versioning:test
```

Integration test suites (`VersioningIntegrationTest`):

| Nested Class               | What it covers                                  |
|----------------------------|-------------------------------------------------|
| `UriPathVersioning`        | V2 routing, V1 deprecation headers              |
| `HeaderVersioning`         | Accept-version routing, default, 400 on unknown |
| `MediaTypeVersioning`      | V1/V2 media types, 406 on unknown               |
| `QueryParameterVersioning` | Report versions, default, 400 on unknown        |
| `JsonViewFieldEvolution`   | V1 hides V2 fields, V2 exposes all              |
| `ETagBehavior`             | 304 on match, different ETags across versions   |
| `DiscoveryAndEnvelope`     | /api/versions payload, envelope shape           |
| `VersionContract`          | Parameterized field contracts, actuator metrics |

`V1DisabledProfileIntegrationTest` (profile `v1-disabled`): verifies 404 on `/v1/employees` and single-entry discovery.
