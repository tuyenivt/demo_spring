package com.example.graphql.exception;

import java.util.UUID;

public class ResourceNotFoundException extends GraphQLException {
    public ResourceNotFoundException(String resourceType, UUID id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, String.format("%s with id '%s' not found", resourceType, id));
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
