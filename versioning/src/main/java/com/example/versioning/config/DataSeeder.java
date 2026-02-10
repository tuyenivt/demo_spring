package com.example.versioning.config;

import com.example.versioning.dto.EmployeeStatus;
import com.example.versioning.entity.Employee;
import com.example.versioning.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        employeeRepository.saveAll(List.of(
                employee("Ada Lovelace", "Principal Engineer", "R&D", LocalDate.parse("2020-04-12"), EmployeeStatus.ACTIVE),
                employee("Grace Hopper", "Engineering Manager", "Platform", LocalDate.parse("2019-07-01"), EmployeeStatus.ACTIVE),
                employee("Margaret Hamilton", "Architect", "Product", LocalDate.parse("2018-11-20"), EmployeeStatus.ON_LEAVE),
                employee("Katherine Johnson", "Data Scientist", "Analytics", LocalDate.parse("2021-02-10"), EmployeeStatus.ACTIVE),
                employee("Alan Turing", "Senior Developer", "Security", LocalDate.parse("2022-03-15"), EmployeeStatus.ON_LEAVE),
                employee("Barbara Liskov", "Staff Engineer", "Core", LocalDate.parse("2017-06-05"), EmployeeStatus.ACTIVE)
        ));
    }

    private Employee employee(String name, String title, String department, LocalDate hireDate, EmployeeStatus status) {
        var employee = new Employee(name, title, department);
        employee.hireDate = hireDate;
        employee.status = status;
        return employee;
    }
}
