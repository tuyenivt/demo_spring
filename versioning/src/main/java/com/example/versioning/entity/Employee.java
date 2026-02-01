package com.example.versioning.entity;

import com.example.versioning.dto.EmployeeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Getter
@EqualsAndHashCode
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public String name;
    public String title;
    @NotNull
    public String department;

    public LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    public EmployeeStatus status;

    protected Employee() {
    }

    public Employee(String name, String title, String department) {
        this.name = name;
        this.title = title;
        this.department = department;
        this.hireDate = LocalDate.now();
        this.status = EmployeeStatus.ACTIVE;
    }
}
