package com.example.graphql.service;

import com.example.graphql.dto.filter.VehicleFilter;
import com.example.graphql.dto.input.CreateVehicleInput;
import com.example.graphql.dto.input.UpdateVehicleInput;
import com.example.graphql.dto.input.UpsertVehicleInput;
import com.example.graphql.dto.pagination.*;
import com.example.graphql.dto.sort.VehicleSort;
import com.example.graphql.entity.Vehicle;
import com.example.graphql.exception.ErrorCode;
import com.example.graphql.exception.ResourceNotFoundException;
import com.example.graphql.exception.TechnicalException;
import com.example.graphql.repository.StudentRepository;
import com.example.graphql.repository.VehicleRepository;
import com.example.graphql.specification.VehicleSpecification;
import com.example.graphql.util.CursorUtils;
import com.example.graphql.util.SortUtils;
import com.example.graphql.validation.VehicleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final StudentRepository studentRepository;
    private final VehicleValidator validator;

    @Transactional
    public Vehicle create(CreateVehicleInput input) {
        validator.validateCreate(input);

        if (input.studentId() != null) {
            var currentVehicleCount = vehicleRepository.countByStudentId(input.studentId());
            validator.validateVehicleLimit(input.studentId(), currentVehicleCount);
        }

        try {
            var vehicle = Vehicle.builder().type(input.type()).build();
            if (input.studentId() != null) {
                var student = studentRepository.findById(input.studentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student", input.studentId()));
                vehicle.setStudent(student);
            }

            return this.vehicleRepository.save(vehicle);
        } catch (DataAccessException e) {
            log.error("Database error while creating vehicle", e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to create vehicle", e);
        }
    }

    @Transactional
    public List<Vehicle> createAll(List<CreateVehicleInput> inputs) {
        inputs.forEach(validator::validateCreate);

        try {
            var vehicles = inputs.stream().map(input -> {
                var vehicle = Vehicle.builder().type(input.type()).build();
                if (input.studentId() != null) {
                    var student = studentRepository.findById(input.studentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Student", input.studentId()));
                    vehicle.setStudent(student);
                }
                return vehicle;
            }).toList();

            return this.vehicleRepository.saveAll(vehicles);
        } catch (DataAccessException e) {
            log.error("Database error while bulk creating vehicles", e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to create vehicles", e);
        }
    }

    @Transactional
    public Vehicle update(UpdateVehicleInput input) {
        validator.validateUpdate(input);

        try {
            var vehicle = this.vehicleRepository.findById(input.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", input.id()));

            if (input.type() != null) {
                vehicle.setType(input.type());
            }
            if (input.studentId() != null) {
                var student = studentRepository.findById(input.studentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student", input.studentId()));
                vehicle.setStudent(student);
            }

            return this.vehicleRepository.save(vehicle);
        } catch (DataAccessException e) {
            log.error("Database error while updating vehicle: {}", input.id(), e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to update vehicle", e);
        }
    }

    @Transactional
    public Vehicle upsert(UpsertVehicleInput input) {
        validator.validateUpsert(input);

        try {
            Vehicle vehicle;
            if (input.id() != null) {
                vehicle = this.vehicleRepository.findById(input.id()).orElseGet(() -> Vehicle.builder().build());
            } else {
                vehicle = Vehicle.builder().build();
            }

            vehicle.setType(input.type());
            if (input.studentId() != null) {
                var student = studentRepository.findById(input.studentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student", input.studentId()));
                vehicle.setStudent(student);
            }

            return this.vehicleRepository.save(vehicle);
        } catch (DataAccessException e) {
            log.error("Database error while upserting vehicle", e);
            throw new TechnicalException(ErrorCode.DATABASE_ERROR, "Failed to upsert vehicle", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll(Integer limit) {
        int actualLimit = limit != null ? limit : 100;
        var pageable = PageRequest.of(0, actualLimit);
        return this.vehicleRepository.findAll(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public PageResult<Vehicle> findPage(PageInput pageInput, VehicleFilter filter, VehicleSort sort) {
        var pageable = PageRequest.of(
                pageInput != null ? pageInput.getPageOrDefault() : PageInput.DEFAULT_PAGE,
                pageInput != null ? pageInput.getSizeOrDefault() : PageInput.DEFAULT_SIZE,
                SortUtils.toSort(sort)
        );
        var spec = VehicleSpecification.fromFilter(filter);
        var page = this.vehicleRepository.findAll(spec, pageable);
        return PageResult.from(page);
    }

    @Transactional(readOnly = true)
    public Connection<Vehicle> findConnection(ConnectionInput connectionInput, VehicleFilter filter, VehicleSort sort) {
        var spec = VehicleSpecification.fromFilter(filter);
        var springSort = SortUtils.toSort(sort);

        long totalCount = this.vehicleRepository.count(spec);

        if (totalCount == 0) {
            return new Connection<>(Collections.emptyList(), new PageInfoConnection(false, false, null, null), 0);
        }

        int limit = connectionInput != null ? connectionInput.getLimit() : ConnectionInput.DEFAULT_LIMIT;
        var afterCursor = connectionInput != null ? CursorUtils.decode(connectionInput.after()) : null;
        var beforeCursor = connectionInput != null ? CursorUtils.decode(connectionInput.before()) : null;

        Specification<Vehicle> cursorSpec = spec;

        if (afterCursor != null) {
            cursorSpec = cursorSpec.and((root, query, cb) ->
                    cb.greaterThan(root.get("id"), afterCursor));
        }
        if (beforeCursor != null) {
            cursorSpec = cursorSpec.and((root, query, cb) ->
                    cb.lessThan(root.get("id"), beforeCursor));
        }

        var pageable = PageRequest.of(0, limit + 1, springSort);
        var results = this.vehicleRepository.findAll(cursorSpec, pageable).getContent();

        boolean hasMore = results.size() > limit;
        var edges = results.stream().limit(limit).map(vehicle -> new Edge<>(vehicle, CursorUtils.encode(vehicle.getId()))).toList();

        var startCursor = edges.isEmpty() ? null : edges.getFirst().cursor();
        var endCursor = edges.isEmpty() ? null : edges.getLast().cursor();

        var pageInfo = new PageInfoConnection(hasMore, afterCursor != null, startCursor, endCursor);

        return new Connection<>(edges, pageInfo, totalCount);
    }
}
