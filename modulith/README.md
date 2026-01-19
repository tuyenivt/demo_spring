# modulith

## Overview

- A modulith Spring demo project.

## Quick Start

### Start MySQL with docker

```shell
docker network create devnet
docker run -d --network devnet --name demodb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=demodb -p 3306:3306 mysql:8.4
```

## API Tests

```shell
curl localhost:8080/api/health
```
