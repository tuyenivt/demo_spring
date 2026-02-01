package com.example.versioning.controller;

import com.example.versioning.service.EmployeeService;
import com.example.versioning.dto.EmployeeResponseV1;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V1 Employee controller with deprecation headers.
 * Demonstrates how to inform clients about deprecated API versions.
 */
@RestController
@RequestMapping("/v1/employees")
public class EmployeeControllerV1 {

    private final EmployeeService employeeService;

    public EmployeeControllerV1(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseV1>> getEmployees() {
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Sunset", "Sat, 31 Dec 2025 23:59:59 GMT")
                .header("Link", "</v2/employees>; rel=\"successor-version\"")
                .body(employeeService.getAllEmployeesV1());
    }
}
