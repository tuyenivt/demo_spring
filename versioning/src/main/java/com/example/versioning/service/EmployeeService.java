package com.example.versioning.service;

import com.example.versioning.dto.EmployeeResponseV1;
import com.example.versioning.dto.EmployeeResponseV2;
import com.example.versioning.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeResponseV1> getAllEmployeesV1() {
        return StreamSupport.stream(employeeRepository.findAll().spliterator(), false)
                .map(e -> new EmployeeResponseV1(e.getId(), e.getName(), e.getDepartment()))
                .toList();
    }

    public List<EmployeeResponseV2> getAllEmployeesV2() {
        return StreamSupport.stream(employeeRepository.findAll().spliterator(), false)
                .map(e -> new EmployeeResponseV2(
                        e.getId(),
                        e.getName(),
                        e.getTitle(),
                        e.getDepartment(),
                        e.getHireDate(),
                        e.getStatus()
                ))
                .toList();
    }
}
