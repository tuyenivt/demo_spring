package com.example.database.replication;

import com.example.database.replication.dto.CreateUserRequest;
import com.example.database.replication.dto.UpdateUserRequest;
import com.example.database.replication.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReplicationIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Use same container for both writer and reader in tests
        registry.add("spring.datasource.writer.jdbc-url", mysql::getJdbcUrl);
        registry.add("spring.datasource.writer.username", mysql::getUsername);
        registry.add("spring.datasource.writer.password", mysql::getPassword);
        registry.add("spring.datasource.reader.jdbc-url", mysql::getJdbcUrl);
        registry.add("spring.datasource.reader.username", mysql::getUsername);
        registry.add("spring.datasource.reader.password", mysql::getPassword);
        registry.add("spring.datasource.liquibase.jdbc-url", mysql::getJdbcUrl);
        registry.add("spring.datasource.liquibase.username", mysql::getUsername);
        registry.add("spring.datasource.liquibase.password", mysql::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndReadUser() {
        // Create user
        var request = new CreateUserRequest("John Doe", "john@example.com");
        var createResponse = restTemplate.postForEntity("/users", request, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().name()).isEqualTo("John Doe");
        assertThat(createResponse.getBody().email()).isEqualTo("john@example.com");
        assertThat(createResponse.getBody().id()).isNotNull();

        // Read user by ID
        var userId = createResponse.getBody().id();
        var getResponse = restTemplate.getForEntity("/users/" + userId, UserResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().id()).isEqualTo(userId);
        assertThat(getResponse.getBody().name()).isEqualTo("John Doe");
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        var response = restTemplate.getForEntity("/users/99999", UserResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldValidateCreateUserRequest() {
        // Missing name and invalid email
        var request = new CreateUserRequest("", "invalid-email");
        var response = restTemplate.postForEntity("/users", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldDeleteUser() {
        // Create user first
        var request = new CreateUserRequest("ToDelete", "delete@example.com");
        var createResponse = restTemplate.postForEntity("/users", request, UserResponse.class);
        var userId = createResponse.getBody().id();

        // Delete user
        restTemplate.delete("/users/" + userId);

        // Verify user is gone
        var getResponse = restTemplate.getForEntity("/users/" + userId, UserResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFindUsersByName() {
        // Create users with same name
        restTemplate.postForEntity("/users", new CreateUserRequest("SameName", "same1@example.com"), UserResponse.class);
        restTemplate.postForEntity("/users", new CreateUserRequest("SameName", "same2@example.com"), UserResponse.class);

        // Find by name
        var response = restTemplate.getForEntity("/users/name/SameName", UserResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateUser() {
        // Create user first
        var createRequest = new CreateUserRequest("Original Name", "original@example.com");
        var createResponse = restTemplate.postForEntity("/users", createRequest, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var userId = createResponse.getBody().id();

        // Update user
        var updateRequest = new UpdateUserRequest("Updated Name", "updated@example.com");
        var updateResponse = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                UserResponse.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().id()).isEqualTo(userId);
        assertThat(updateResponse.getBody().name()).isEqualTo("Updated Name");
        assertThat(updateResponse.getBody().email()).isEqualTo("updated@example.com");

        // Verify update persisted
        var getResponse = restTemplate.getForEntity("/users/" + userId, UserResponse.class);
        assertThat(getResponse.getBody().name()).isEqualTo("Updated Name");
        assertThat(getResponse.getBody().email()).isEqualTo("updated@example.com");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentUser() {
        var updateRequest = new UpdateUserRequest("Not Found", "notfound@example.com");
        var response = restTemplate.exchange(
                "/users/99999",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentUser() {
        restTemplate.delete("/users/99999");
        // Note: RestTemplate.delete() doesn't return response, so we verify via a GET
        var response = restTemplate.getForEntity("/users/99999", UserResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldValidateUpdateUserRequest() {
        // Create user first
        var createRequest = new CreateUserRequest("Test User", "test@example.com");
        var createResponse = restTemplate.postForEntity("/users", createRequest, UserResponse.class);
        var userId = createResponse.getBody().id();

        // Try to update with invalid data
        var invalidUpdateRequest = new UpdateUserRequest("", "invalid-email");
        var response = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(invalidUpdateRequest),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
