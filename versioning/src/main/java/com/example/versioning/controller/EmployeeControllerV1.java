package com.example.versioning.controller;

import com.example.versioning.dto.EmployeeResponseV1;
import com.example.versioning.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V1 Employee controller with deprecation headers.
 * Demonstrates how to inform clients about deprecated API versions.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/employees")
@ConditionalOnProperty(name = "api.v1.enabled", havingValue = "true", matchIfMissing = true)
public class EmployeeControllerV1 {

    private final EmployeeService employeeService;

    @GetMapping
    @Deprecated
    @Operation(summary = "Get all employees (v1)", deprecated = true)
    @ApiResponse(responseCode = "200", description = "Returns v1 employee schema")
    public ResponseEntity<List<EmployeeResponseV1>> getEmployees() {
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Sunset", "Sat, 31 Dec 2025 23:59:59 GMT")
                .header("Link", "</v2/employees>; rel=\"successor-version\"")
                .body(employeeService.getAllEmployeesV1());
    }

    @GetMapping("/{id}")
    @Deprecated
    @Operation(summary = "Get one employee (v1)", deprecated = true)
    @ApiResponse(responseCode = "200", description = "Returns v1 employee schema")
    public ResponseEntity<EmployeeResponseV1> getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeV1(id)
                .map(employee -> ResponseEntity.ok()
                        .header("Deprecation", "true")
                        .header("Sunset", "Sat, 31 Dec 2025 23:59:59 GMT")
                        .header("Link", "</v2/employees>; rel=\"successor-version\"")
                        .body(employee))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
