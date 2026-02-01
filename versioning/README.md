# Demo Spring Versioning

## API Versioning Strategies

This project demonstrates three common API versioning strategies:

### 1. URI Path Versioning
Simplest approach - version is part of the URI path.
```
/v1/employees
/v2/employees
```

### 2. Custom Header Versioning
Single URI with version specified via custom HTTP header.
```
GET /location
Accept-version: v2
```

### 3. Media Type Versioning (Content Negotiation)
Uses vendor-specific MIME types in Accept header.
```
GET /api/products
Accept: application/vnd.company.v2+json
```

## Managing Versions

- Grouping breaking changes into a single release (major version like v1 and v2)
- Set an end-of-life date for the previous version when a new version released
- Continue to bug fix and independently deploy old versions of the API behind a load balancer to route the traffic based on rules
  - URI: look for v1 or v2 in the URI path
  - Custom Header: look for custom HTTP header you defined and route calls based on the defined values you have set
- Apply fixes from the new version to the old version using cherry-pick

## Versioning Best Practices

- **Semantic Versioning**: Use major versions for breaking changes only
- **Deprecation Timeline**: Announce deprecation 6+ months before sunset
- **Deprecation Headers**: Include `Deprecation`, `Sunset`, and `Link` headers
- **Changelog**: Maintain per-version changelog
- **Migration Guide**: Provide upgrade paths between versions
- **Version Discovery**: Add endpoint to list available versions (`/api/versions`)

## Versioning

- Version 2: **master** branch
- Version 1: **v1** branch

## V2 Examples

```bash
git checkout master

# health check
curl --location --request GET 'http://localhost:8080/actuator/health'

# get all employees (Spring Data REST)
curl --location --request GET 'http://localhost:8080/v2/employees'

# create a new employee
curl --location --request POST 'http://localhost:8080/v2/employees' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "tuyen",
    "title": "developer",
    "department": "sd"
}'

# get schedule (URI path versioning)
curl --location --request GET 'http://localhost:8080/v2/schedule'

# get location (custom header versioning)
curl --location --request GET 'http://localhost:8080/location' \
--header 'Accept-version: v2'

# get product V2 (media type versioning)
curl --location --request GET 'http://localhost:8080/api/products' \
--header 'Accept: application/vnd.company.v2+json'

# get product V1 (media type versioning)
curl --location --request GET 'http://localhost:8080/api/products' \
--header 'Accept: application/vnd.company.v1+json'

# get employees with V2 DTOs
curl --location --request GET 'http://localhost:8080/api/v2/employees'

# discover available versions
curl --location --request GET 'http://localhost:8080/api/versions'
```

## V1 (Deprecated)

V1 endpoints include deprecation headers to inform clients.

```bash
git checkout v1

# health check
curl --location --request GET 'http://localhost:8080/actuator/health'

# get all employees (includes deprecation headers)
curl -i --location --request GET 'http://localhost:8080/v1/employees'
# Response headers:
#   Deprecation: true
#   Sunset: Sat, 31 Dec 2025 23:59:59 GMT
#   Link: </v2/employees>; rel="successor-version"

# get all employees
curl --location --request GET 'http://localhost:8080/v1/employees'

# create a new employee
curl --location --request POST 'http://localhost:8080/v1/employees' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "spring",
    "title": "developer"
}'

# get schedule
curl --location --request GET 'http://localhost:8080/v1/schedule'
```

## Running

```bash
./gradlew :versioning:bootRun
```

## Testing

```bash
./gradlew :versioning:test
```
