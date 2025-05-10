# Demo Spring Versioning

### API Versioning Strategies

- URI - simplicity (versioning in URI): /v1, /v2
- Custom Header - single URI (keep versioning out of the URI): Accept-version: v1, Accept-version: v2

### Managing Versions

- Grouping breaking changes into a single release (major version like v1 and v2)
- Set an end-of-life date for the previous version when a new version released
- Continue to bug fix and independently deploy old versions of the API behind a load balancer to route the traffic based on rules
  - URI: look for v1 or v2 in the URI path
  - Custom Header: look for custom HTTP header you defined and route calls based on the defined values you have set
- Apply fixes from the new version to the old version using cherry-pick

## Versioning

- Version 2: **master** branch
- Version 1: **v1** branch

## V2

```bash
git checkout master
# health check
curl --location --request GET 'http://localhost:8080/actuator/health'
# get all employees
curl --location --request GET 'http://localhost:8080/v2/employees'
# create a new employee
curl --location --request POST 'http://localhost:8080/v2/employees' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "tuyen",
    "title": "developer",
    "department": "sd"
}'
# get schedule
curl --location --request GET 'http://localhost:8080/v2/schedule'
# get location (custom header)
curl --location --request GET 'http://localhost:8080/location' \
--header 'Accept-version: v2'
```

## V1

```bash
git checkout v1
# health check
curl --location --request GET 'http://localhost:8080/actuator/health'
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
