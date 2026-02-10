package com.example.versioning.controller;

import com.example.versioning.dto.EmployeeResponseV2;
import com.example.versioning.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V2 Employee controller - current stable version.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/employees")
public class EmployeeControllerV2 {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all employees (v2)")
    @ApiResponse(responseCode = "200", description = "Returns v2 employee schema")
    public ResponseEntity<List<EmployeeResponseV2>> getEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployeesV2());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one employee (v2)")
    @ApiResponse(responseCode = "200", description = "Returns v2 employee schema")
    public ResponseEntity<EmployeeResponseV2> getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeV2(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
