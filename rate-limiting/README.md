# Rate Limiting Demo

## Quick Start

```bash
docker run -d --name redis -p 6379:6379 redis:8.4-alpine
```

## API Tests

```bash
curl localhost:8080/api/hello
curl localhost:8080/api/orders -H "X-USER-ID: 12345"
```
