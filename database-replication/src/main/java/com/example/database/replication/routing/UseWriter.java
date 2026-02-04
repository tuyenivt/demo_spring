package com.example.database.replication.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should use the writer datasource.
 * Methods without this annotation will use the reader datasource by default.
 *
 * <p>Usage example:
 * <pre>
 * &#64;UseWriter
 * public User createUser(CreateUserRequest request) {
 *     return userRepository.save(user);
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseWriter {
}
