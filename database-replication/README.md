# Spring AOP Demo

## Quick Start

### 

```bash
# Run database replication simulation
cd docker
docker compose up -d

# Clean up
docker compose down --volumes --remove-orphans
```

## API Tests

Create user

```bash
curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{
    "name": "Test User",
    "email": "test.user@example.com"
  }'
```

Get a user

```bash
curl http://localhost:8080/users/1
```

Find all users by name

```bash
curl "http://localhost:8080/users/name/Test%20User"
```

Delete a user

```bash
curl -X DELETE http://localhost:8080/users/1
```
