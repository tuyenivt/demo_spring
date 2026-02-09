package com.example.graphql.controller;

import com.example.graphql.dto.filter.StudentFilter;
import com.example.graphql.dto.input.CreateStudentInput;
import com.example.graphql.dto.input.UpdateStudentInput;
import com.example.graphql.dto.input.UpsertStudentInput;
import com.example.graphql.dto.pagination.Connection;
import com.example.graphql.dto.pagination.ConnectionInput;
import com.example.graphql.dto.pagination.PageInput;
import com.example.graphql.dto.pagination.PageResult;
import com.example.graphql.dto.sort.StudentSort;
import com.example.graphql.entity.Student;
import com.example.graphql.entity.Vehicle;
import com.example.graphql.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;

    @QueryMapping
    public Student student(@Argument String id) {
        return this.service.findById(id);
    }

    @Deprecated
    @QueryMapping
    public List<Student> students(@Argument Integer limit) {
        return this.service.findAll(limit);
    }

    @QueryMapping
    public PageResult<Student> studentsPage(
            @Argument PageInput page,
            @Argument StudentFilter filter,
            @Argument StudentSort sort) {
        return this.service.findPage(page, filter, sort);
    }

    @QueryMapping
    public Connection<Student> studentsConnection(
            @Argument ConnectionInput connection,
            @Argument StudentFilter filter,
            @Argument StudentSort sort) {
        return this.service.findConnection(connection, filter, sort);
    }

    @MutationMapping
    public Student createStudent(@Argument CreateStudentInput input) {
        return this.service.create(input);
    }

    @MutationMapping
    public List<Student> createStudents(@Argument List<CreateStudentInput> inputs) {
        return this.service.createAll(inputs);
    }

    @MutationMapping
    public Student updateStudent(@Argument UpdateStudentInput input) {
        return this.service.update(input);
    }

    @MutationMapping
    public Student upsertStudent(@Argument UpsertStudentInput input) {
        return this.service.upsert(input);
    }

    @BatchMapping
    public Map<Student, List<Vehicle>> vehicles(List<Student> students) {
        return this.service.findVehiclesForStudents(students);
    }
}
