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
        var student = new Student();
        student.setName(input.name());
        student.setAddress(input.address());
        if (input.dateOfBirth() != null) {
            student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
        }
        return this.repository.save(student);
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
