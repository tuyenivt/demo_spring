package com.example.graphql.service;

import com.example.graphql.dto.filter.StudentFilter;
import com.example.graphql.dto.input.CreateStudentInput;
import com.example.graphql.dto.input.UpdateStudentInput;
import com.example.graphql.dto.input.UpsertStudentInput;
import com.example.graphql.dto.pagination.*;
import com.example.graphql.dto.sort.StudentSort;
import com.example.graphql.entity.Student;
import com.example.graphql.exception.ErrorCode;
import com.example.graphql.exception.ResourceNotFoundException;
import com.example.graphql.exception.TechnicalException;
import com.example.graphql.repository.StudentRepository;
import com.example.graphql.specification.StudentSpecification;
import com.example.graphql.util.CursorUtils;
import com.example.graphql.util.SortUtils;
import com.example.graphql.validation.StudentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository repository;
    private final StudentValidator validator;

    @Transactional
    public Student create(CreateStudentInput input) {
        validator.validateCreate(input);

        try {
            var student = Student.builder()
                    .name(input.name().trim())
                    .address(input.address() != null ? input.address().trim() : null)
                    .build();

            if (input.dateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
            }

            return this.repository.save(student);
        } catch (DataAccessException e) {
            log.error("Database error while creating student", e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to create student", e);
        }
    }

    @Transactional
    public List<Student> createAll(List<CreateStudentInput> inputs) {
        inputs.forEach(validator::validateCreate);

        try {
            var students = inputs.stream().map(input -> {
                var student = Student.builder()
                        .name(input.name().trim())
                        .address(input.address() != null ? input.address().trim() : null)
                        .build();
                if (input.dateOfBirth() != null) {
                    student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
                }
                return student;
            }).toList();

            return this.repository.saveAll(students);
        } catch (DataAccessException e) {
            log.error("Database error while bulk creating students", e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to create students", e);
        }
    }

    @Transactional
    public Student update(UpdateStudentInput input) {
        validator.validateUpdate(input);

        try {
            var student = this.repository.findById(input.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", input.id()));

            if (input.name() != null) {
                student.setName(input.name().trim());
            }
            if (input.address() != null) {
                student.setAddress(input.address().trim());
            }
            if (input.dateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
            }

            return this.repository.save(student);
        } catch (DataAccessException e) {
            log.error("Database error while updating student: {}", input.id(), e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to update student", e);
        }
    }

    @Transactional
    public Student upsert(UpsertStudentInput input) {
        validator.validateUpsert(input);

        try {
            Student student;
            if (input.id() != null) {
                student = this.repository.findById(input.id()).orElseGet(() -> Student.builder().build());
            } else {
                student = Student.builder().build();
            }

            student.setName(input.name().trim());
            if (input.address() != null) {
                student.setAddress(input.address().trim());
            }
            if (input.dateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
            }

            return this.repository.save(student);
        } catch (DataAccessException e) {
            log.error("Database error while upserting student", e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to upsert student", e);
        }
    }

    @Transactional(readOnly = true)
    public Student findById(String id) {
        try {
            var uuid = UUID.fromString(id);
            return this.repository.findById(uuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", uuid));
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid student ID format: " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<Student> findAll(Integer limit) {
        int actualLimit = limit != null ? limit : 100;
        var pageable = PageRequest.of(0, actualLimit);
        return this.repository.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public PageResult<Student> findPage(PageInput pageInput, StudentFilter filter, StudentSort sort) {
        var pageable = PageRequest.of(
                pageInput != null ? pageInput.getPageOrDefault() : PageInput.DEFAULT_PAGE,
                pageInput != null ? pageInput.getSizeOrDefault() : PageInput.DEFAULT_SIZE,
                SortUtils.toSort(sort)
        );
        var spec = StudentSpecification.fromFilter(filter);
        var page = this.repository.findAll(spec, pageable);
        return PageResult.from(page);
    }

    @Transactional(readOnly = true)
    public Connection<Student> findConnection(ConnectionInput connectionInput, StudentFilter filter, StudentSort sort) {
        var spec = StudentSpecification.fromFilter(filter);
        var springSort = SortUtils.toSort(sort);

        long totalCount = this.repository.count(spec);

        if (totalCount == 0) {
            return new Connection<>(Collections.emptyList(), new PageInfoConnection(false, false, null, null), 0);
        }

        int limit = connectionInput != null ? connectionInput.getLimit() : ConnectionInput.DEFAULT_LIMIT;
        var afterCursor = connectionInput != null ? CursorUtils.decode(connectionInput.after()) : null;
        var beforeCursor = connectionInput != null ? CursorUtils.decode(connectionInput.before()) : null;

        Specification<Student> cursorSpec = spec;

        if (afterCursor != null) {
            cursorSpec = cursorSpec.and((root, query, cb) ->
                    cb.greaterThan(root.get("id"), afterCursor));
        }
        if (beforeCursor != null) {
            cursorSpec = cursorSpec.and((root, query, cb) ->
                    cb.lessThan(root.get("id"), beforeCursor));
        }

        var pageable = PageRequest.of(0, limit + 1, springSort);
        var results = this.repository.findAll(cursorSpec, pageable).getContent();

        boolean hasMore = results.size() > limit;
        var edges = results.stream().limit(limit).map(student -> new Edge<>(student, CursorUtils.encode(student.getId()))).toList();

        var startCursor = edges.isEmpty() ? null : edges.getFirst().cursor();
        var endCursor = edges.isEmpty() ? null : edges.getLast().cursor();

        var pageInfo = new PageInfoConnection(hasMore, afterCursor != null, startCursor, endCursor);

        return new Connection<>(edges, pageInfo, totalCount);
    }
}
