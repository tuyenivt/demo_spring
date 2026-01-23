package com.coloza.demo.graphql.dto.sort;

public enum StudentSortField {
    NAME("name"),
    ADDRESS("address"),
    DATE_OF_BIRTH("dateOfBirth"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String fieldName;

    StudentSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
