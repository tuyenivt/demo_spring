package com.coloza.demo.graphql.controller;

import com.coloza.demo.graphql.model.entity.Student;
import com.coloza.demo.graphql.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;

    @QueryMapping
    public Optional<Student> getStudent(int id) {
        return this.service.findById(id);
    }

    @QueryMapping
    public List<Student> getStudents(int limit) {
        return this.service.findAll(limit);
    }

    @MutationMapping
    public Student createStudent(String name, String address, String dateOfBirth) {
        return this.service.create(name, address, dateOfBirth);
    }
}
