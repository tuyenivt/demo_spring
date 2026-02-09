package com.example.graphql.validation;

import com.example.graphql.dto.input.CreateVehicleInput;
import com.example.graphql.dto.input.UpdateVehicleInput;
import com.example.graphql.dto.input.UpsertVehicleInput;
import com.example.graphql.enums.VehicleType;
import com.example.graphql.exception.ErrorCode;
import com.example.graphql.exception.ValidationException;
import com.example.graphql.repository.StudentRepository;
import com.example.graphql.util.AgeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehicleValidator {

    private final StudentRepository studentRepository;

    private static final int MIN_DRIVING_AGE = 16;
    private static final int MIN_MOTORCYCLE_AGE = 18;

    public void validateCreate(CreateVehicleInput input) {
        Map<String, String> errors = new HashMap<>();

        if (input.type() == null) {
            errors.put("type", "Vehicle type is required");
        }

        if (input.studentId() != null) {
            validateStudentAssignment(input.studentId(), input.type(), errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Vehicle creation validation failed", errors);
        }
    }

    public void validateUpdate(UpdateVehicleInput input) {
        Map<String, String> errors = new HashMap<>();

        if (input.id() == null) {
            errors.put("id", "Vehicle ID is required for update");
        }

        if (input.studentId() != null) {
            validateStudentAssignment(input.studentId(), input.type(), errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Vehicle update validation failed", errors);
        }
    }

    public void validateUpsert(UpsertVehicleInput input) {
        Map<String, String> errors = new HashMap<>();

        if (input.type() == null) {
            errors.put("type", "Vehicle type is required");
        }

        if (input.studentId() != null) {
            validateStudentAssignment(input.studentId(), input.type(), errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Vehicle upsert validation failed", errors);
        }
    }

    private void validateStudentAssignment(UUID studentId, VehicleType vehicleType, Map<String, String> errors) {
        var studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            errors.put("studentId", String.format("Student with id '%s' not found", studentId));
            return;
        }

        var student = studentOpt.get();

        if (student.getDateOfBirth() == null) {
            throw new ValidationException(ErrorCode.VEHICLE_ASSIGNMENT_ERROR, "Cannot assign vehicle to student without a date of birth");
        }

        var age = AgeUtils.calculateAge(student.getDateOfBirth());

        if (vehicleType != null) {
            validateAgeForVehicleType(vehicleType, age, student.getName());
        }
    }

    private void validateAgeForVehicleType(VehicleType vehicleType, int studentAge, String studentName) {
        switch (vehicleType) {
            case CAR, TRUCK, VAN, BUS -> {
                if (studentAge < MIN_DRIVING_AGE) {
                    throw new ValidationException(
                            ErrorCode.VEHICLE_ASSIGNMENT_ERROR,
                            String.format("Student '%s' (age %d) is too young to have a %s. Minimum age: %d",
                                    studentName, studentAge, vehicleType.name().toLowerCase(), MIN_DRIVING_AGE)
                    );
                }
            }
            case MOTORCYCLE, SCOOTER -> {
                if (studentAge < MIN_MOTORCYCLE_AGE) {
                    throw new ValidationException(
                            ErrorCode.VEHICLE_ASSIGNMENT_ERROR,
                            String.format("Student '%s' (age %d) is too young to have a %s. Minimum age: %d",
                                    studentName, studentAge, vehicleType.name().toLowerCase(), MIN_MOTORCYCLE_AGE)
                    );
                }
            }
            case BICYCLE -> {
                // No age restriction for bicycles
            }
        }
    }

    public void validateVehicleLimit(UUID studentId, int currentVehicleCount) {
        final int MAX_VEHICLES_PER_STUDENT = 5;

        if (currentVehicleCount >= MAX_VEHICLES_PER_STUDENT) {
            throw new ValidationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    String.format("Student %s already has maximum allowed vehicles (%d)", studentId, MAX_VEHICLES_PER_STUDENT)
            );
        }
    }
}
