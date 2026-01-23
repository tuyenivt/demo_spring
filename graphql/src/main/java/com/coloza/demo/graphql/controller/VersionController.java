package com.coloza.demo.graphql.controller;

import com.coloza.demo.graphql.dto.versioning.ApiVersion;
import com.coloza.demo.graphql.dto.versioning.StudentV1;
import com.coloza.demo.graphql.dto.versioning.VehicleV1;
import com.coloza.demo.graphql.service.StudentService;
import com.coloza.demo.graphql.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class VersionController {

    private final StudentService studentService;
    private final VehicleService vehicleService;

    @QueryMapping
    public ApiVersion apiVersion() {
        return new ApiVersion(
                "2.0",
                List.of(
                        "students query - use studentsPage instead",
                        "vehicles query - use vehiclesPage instead",
                        "String type for Vehicle.type - use VehicleType enum instead",
                        "V1 types (StudentV1, VehicleV1) - use current types instead"
                ),
                "2025-12-31"
        );
    }

    @Deprecated
    @QueryMapping
    public StudentV1 studentV1(@Argument String id) {
        return StudentV1.from(studentService.findById(id));
    }

    @Deprecated
    @QueryMapping
    public List<StudentV1> studentsV1(@Argument Integer limit) {
        return studentService.findAll(limit).stream()
                .map(StudentV1::from)
                .toList();
    }

    @Deprecated
    @QueryMapping
    public List<VehicleV1> vehiclesV1(@Argument Integer limit) {
        return vehicleService.findAll(limit).stream()
                .map(VehicleV1::from)
                .toList();
    }
}
