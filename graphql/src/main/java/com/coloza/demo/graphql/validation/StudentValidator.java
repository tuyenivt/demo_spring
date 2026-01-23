package com.coloza.demo.graphql.validation;

import com.coloza.demo.graphql.dto.CreateStudentInput;
import com.coloza.demo.graphql.dto.UpdateStudentInput;
import com.coloza.demo.graphql.dto.UpsertStudentInput;
import com.coloza.demo.graphql.exception.ErrorCode;
import com.coloza.demo.graphql.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Component
public class StudentValidator {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_AGE = 5;
    private static final int MAX_AGE = 100;

    public void validateCreate(CreateStudentInput input) {
        Map<String, String> errors = new HashMap<>();

        validateName(input.name(), errors);
        validateAddress(input.address(), errors);
        validateDateOfBirth(input.dateOfBirth(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("Student creation validation failed", errors);
        }
    }

    public void validateUpdate(UpdateStudentInput input) {
        Map<String, String> errors = new HashMap<>();

        if (input.id() == null) {
            errors.put("id", "Student ID is required for update");
        }

        if (input.name() != null) {
            validateName(input.name(), errors);
        }

        if (input.address() != null) {
            validateAddress(input.address(), errors);
        }

        if (input.dateOfBirth() != null) {
            validateDateOfBirth(input.dateOfBirth(), errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Student update validation failed", errors);
        }
    }

    public void validateUpsert(UpsertStudentInput input) {
        Map<String, String> errors = new HashMap<>();

        validateName(input.name(), errors);
        validateAddress(input.address(), errors);
        validateDateOfBirth(input.dateOfBirth(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("Student upsert validation failed", errors);
        }
    }

    private void validateName(String name, Map<String, String> errors) {
        if (name == null || name.isBlank()) {
            errors.put("name", "Name is required");
            return;
        }

        var trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            errors.put("name", String.format("Name must be at least %d characters", MIN_NAME_LENGTH));
        } else if (trimmedName.length() > MAX_NAME_LENGTH) {
            errors.put("name", String.format("Name must not exceed %d characters", MAX_NAME_LENGTH));
        } else if (!trimmedName.matches("^[a-zA-Z\\s'-]+$")) {
            errors.put("name", "Name can only contain letters, spaces, hyphens, and apostrophes");
        }
    }

    private void validateAddress(String address, Map<String, String> errors) {
        if (address != null && address.trim().length() > 200) {
            errors.put("address", "Address must not exceed 200 characters");
        }
    }

    private void validateDateOfBirth(String dateOfBirth, Map<String, String> errors) {
        if (dateOfBirth == null) {
            return;
        }

        try {
            var dob = LocalDate.parse(dateOfBirth);
            var now = LocalDate.now();

            if (dob.isAfter(now)) {
                errors.put("dateOfBirth", "Date of birth cannot be in the future");
                return;
            }

            var age = now.getYear() - dob.getYear();
            if (dob.plusYears(age).isAfter(now)) {
                age--;
            }

            if (age < MIN_AGE) {
                throw new ValidationException(
                        ErrorCode.STUDENT_AGE_INVALID,
                        String.format("Student must be at least %d years old (current age: %d)", MIN_AGE, age)
                );
            }

            if (age > MAX_AGE) {
                throw new ValidationException(
                        ErrorCode.STUDENT_AGE_INVALID,
                        String.format("Invalid age: %d years. Maximum allowed age is %d", age, MAX_AGE)
                );
            }

        } catch (DateTimeParseException e) {
            errors.put("dateOfBirth", "Invalid date format. Expected format: YYYY-MM-DD");
        }
    }
}
