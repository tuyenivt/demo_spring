package com.example.versioning.controller;

import com.example.versioning.service.EmployeeService;
import com.example.versioning.dto.EmployeeResponseV2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V2 Employee controller - current stable version.
 */
@RestController
@RequestMapping("/api/v2/employees")
public class EmployeeControllerV2 {

    private final EmployeeService employeeService;

    public EmployeeControllerV2(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseV2>> getEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployeesV2());
    }
}
