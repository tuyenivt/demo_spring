package com.coloza.demo.graphql.service;

import com.coloza.demo.graphql.dto.CreateStudentInput;
import com.coloza.demo.graphql.dto.UpdateStudentInput;
import com.coloza.demo.graphql.dto.UpsertStudentInput;
import com.coloza.demo.graphql.dto.filter.StudentFilter;
import com.coloza.demo.graphql.dto.pagination.*;
import com.coloza.demo.graphql.dto.sort.StudentSort;
import com.coloza.demo.graphql.entity.Student;
import com.coloza.demo.graphql.repository.StudentRepository;
import com.coloza.demo.graphql.specification.StudentSpecification;
import com.coloza.demo.graphql.util.CursorUtils;
import com.coloza.demo.graphql.util.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
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

    @Transactional
    public Optional<Student> update(UpdateStudentInput input) {
        return this.repository.findById(input.id()).map(student -> {
            if (input.name() != null) {
                student.setName(input.name());
            }
            if (input.address() != null) {
                student.setAddress(input.address());
            }
            if (input.dateOfBirth() != null) {
                student.setDateOfBirth(LocalDate.parse(input.dateOfBirth()));
            }
            return this.repository.save(student);
        });
    }

    @Transactional
    public Student upsert(UpsertStudentInput input) {
        Student student;
        if (input.id() != null) {
            student = this.repository.findById(input.id()).orElseGet(() -> Student.builder().build());
        } else {
            student = Student.builder().build();
        }
        student.setName(input.name());
        if (input.address() != null) {
            student.setAddress(input.address());
        }
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
