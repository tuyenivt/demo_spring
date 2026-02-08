package com.example.database.migration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false"  // Disable scheduled tasks in tests
})
@Testcontainers
class MainApplicationTests {

    @Container
    static MySQLContainer<?> sourceMysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("demo")
            .withUsername("root")
            .withPassword("root");

    @Container
    static MySQLContainer<?> targetMysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("demo")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("old-demo.datasource.jdbcUrl", sourceMysql::getJdbcUrl);
        registry.add("old-demo.datasource.username", sourceMysql::getUsername);
        registry.add("old-demo.datasource.password", sourceMysql::getPassword);

        registry.add("demo.datasource.jdbcUrl", targetMysql::getJdbcUrl);
        registry.add("demo.datasource.username", targetMysql::getUsername);
        registry.add("demo.datasource.password", targetMysql::getPassword);
    }

    @Test
    void contextLoads() {
        // Verify that Spring context loads successfully with dual datasources and Flyway migrations
        // Flyway will automatically create all necessary tables in both databases
    }
}
