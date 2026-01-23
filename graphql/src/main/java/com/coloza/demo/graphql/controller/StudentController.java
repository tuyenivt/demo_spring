package com.coloza.demo.graphql.controller;

import com.coloza.demo.graphql.dto.CreateStudentInput;
import com.coloza.demo.graphql.dto.UpdateStudentInput;
import com.coloza.demo.graphql.dto.UpsertStudentInput;
import com.coloza.demo.graphql.dto.filter.StudentFilter;
import com.coloza.demo.graphql.dto.pagination.Connection;
import com.coloza.demo.graphql.dto.pagination.ConnectionInput;
import com.coloza.demo.graphql.dto.pagination.PageInput;
import com.coloza.demo.graphql.dto.pagination.PageResult;
import com.coloza.demo.graphql.dto.sort.StudentSort;
import com.coloza.demo.graphql.entity.Student;
import com.coloza.demo.graphql.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
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
    public Optional<Student> student(@Argument String id) {
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
    public Student createStudent(@Argument @Valid CreateStudentInput input) {
        return this.service.create(input);
    }

    @MutationMapping
    public List<Student> createStudents(@Argument @Valid List<CreateStudentInput> inputs) {
        return this.service.createAll(inputs);
    }

    @MutationMapping
    public Optional<Student> updateStudent(@Argument @Valid UpdateStudentInput input) {
        return this.service.update(input);
    }

    @MutationMapping
    public Student upsertStudent(@Argument @Valid UpsertStudentInput input) {
        return this.service.upsert(input);
    }
}
