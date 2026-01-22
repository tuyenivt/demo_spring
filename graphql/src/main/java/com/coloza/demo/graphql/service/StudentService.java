package com.coloza.demo.graphql.service;

import com.coloza.demo.graphql.entity.Student;
import com.coloza.demo.graphql.dto.CreateStudentInput;
import com.coloza.demo.graphql.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository repository;

    @Transactional
    public Student create(CreateStudentInput input) {
        var student = Student.builder().name(input.name()).address(input.address()).build();
        if (input.dateOfBirth() != null) {
            student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
        }
        return this.repository.save(student);
    }

    @Transactional
    public List<Student> createAll(List<CreateStudentInput> inputs) {
        var students = inputs.stream().map(input -> {
            var student = Student.builder().name(input.name()).address(input.address()).build();
            if (input.dateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
            }
            return student;
        }).toList();
        return this.repository.saveAll(students);
    }

    @Transactional(readOnly = true)
    public Optional<Student> findById(String id) {
        return this.repository.findById(UUID.fromString(id));
    }

    @Transactional(readOnly = true)
    public List<Student> findAll(Integer limit) {
        var stream = this.repository.findAll().stream();
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream.toList();
    }
}
