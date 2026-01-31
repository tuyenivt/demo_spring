# Multiple Databases Subproject

## Overview

Spring Boot data migration application that continuously synchronizes product data from a legacy database to a new database with schema transformation.

## Project Structure

```
multiple-databases/
├── src/main/java/com/example/multiple/databases/
│   ├── MainApplication.java          # Entry point with @EnableScheduling
│   ├── config/
│   │   └── AppConfig.java            # Batch size configuration
│   ├── demo/                         # Target database (port 3307)
│   │   ├── config/DemoDataSourceConfiguration.java
│   │   ├── entity/Product.java
│   │   ├── mapper/ProductMapper.java
│   │   ├── repository/ProductRepository.java
│   │   └── task/ProductTasks.java    # Migration scheduler
│   └── oldDemo/                      # Source database (port 3306)
│       ├── config/OldDemoDataSourceConfiguration.java
│       ├── entity/OldProduct.java
│       └── repository/OldProductRepository.java
└── src/main/resources/
    └── application.yml               # Dual datasource config
```

## Architecture

### Dual DataSource Configuration

- **Demo (Primary)**: `jdbc:mysql://localhost:3307/demo` - Target database
- **OldDemo (Secondary)**: `jdbc:mysql://localhost:3306/demo` - Source database

Each database has isolated:
- DataSource bean
- EntityManagerFactory
- TransactionManager
- Repository scanning

### Entity Mapping

| OldProduct (Source) | Product (Target)     | Transformation          |
|---------------------|----------------------|-------------------------|
| productId           | productId            | Direct copy             |
| productName         | productName          | Direct copy             |
| price               | price                | Direct copy             |
| quality             | inStock              | Renamed, default 0      |
| dateOfManufacture   | dateOfManufacture    | Direct copy             |
| updatedAt           | updatedAt            | Timezone adjust (-7h)   |
| -                   | vendor               | Constant "ABC"          |

### Migration Flow

1. Read last sync timestamp from `updatedAt_Product.txt`
2. Query OldDemo for records with `updatedAt > lastSync`
3. Process in batches (default: 100 records)
4. Transform via MapStruct mapper
5. Batch save to Demo database
6. Update state file with latest timestamp

## Key Files

| File | Purpose |
|------|---------|
| `ProductTasks.java:23` | Scheduled migration task entry |
| `ProductMapper.java:10` | MapStruct entity transformation |
| `DemoDataSourceConfiguration.java:17` | Primary datasource setup |
| `OldDemoDataSourceConfiguration.java:17` | Secondary datasource setup |
| `application.yml:1` | All configuration properties |

## Configuration

```yaml
# Key settings in application.yml
spring.datasource.demo.url: jdbc:mysql://localhost:3307/demo
spring.datasource.oldDemo.url: jdbc:mysql://localhost:3306/demo
spring.jpa.properties.hibernate.jdbc.batch_size: 100
scheduled.fixedRate: 10000  # 10 seconds
```

## Dependencies

- Spring Boot Starter Data JPA
- MapStruct (entity mapping)
- MySQL Connector/J
- Lombok

## Running

Requires two MySQL instances:
- Port 3306: Source database with `old_product` table
- Port 3307: Target database with `product` table

```bash
./gradlew :multiple-databases:bootRun
```

## State Management

Migration state persisted in `updatedAt_Product.txt` at project root. Delete this file to restart migration from beginning.
