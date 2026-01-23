package com.coloza.demo.graphql.service;

import com.coloza.demo.graphql.dto.CreateVehicleInput;
import com.coloza.demo.graphql.dto.UpdateVehicleInput;
import com.coloza.demo.graphql.dto.UpsertVehicleInput;
import com.coloza.demo.graphql.dto.filter.VehicleFilter;
import com.coloza.demo.graphql.dto.pagination.*;
import com.coloza.demo.graphql.dto.sort.VehicleSort;
import com.coloza.demo.graphql.entity.Vehicle;
import com.coloza.demo.graphql.repository.StudentRepository;
import com.coloza.demo.graphql.repository.VehicleRepository;
import com.coloza.demo.graphql.specification.VehicleSpecification;
import com.coloza.demo.graphql.util.CursorUtils;
import com.coloza.demo.graphql.util.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public Vehicle create(CreateVehicleInput input) {
        var vehicle = Vehicle.builder().type(input.type()).build();
        if (input.studentId() != null) {
            var student = studentRepository.findById(input.studentId());
            student.ifPresent(vehicle::setStudent);
        }
        return this.vehicleRepository.save(vehicle);
    }

    @Transactional
    public List<Vehicle> createAll(List<CreateVehicleInput> inputs) {
        var vehicles = inputs.stream().map(input -> {
            var vehicle = Vehicle.builder().type(input.type()).build();
            if (input.studentId() != null) {
                var student = studentRepository.findById(input.studentId());
                student.ifPresent(vehicle::setStudent);
            }
            return vehicle;
        }).toList();
        return this.vehicleRepository.saveAll(vehicles);
    }

    @Transactional
    public Optional<Vehicle> update(UpdateVehicleInput input) {
        return this.vehicleRepository.findById(input.id()).map(vehicle -> {
            if (input.type() != null) {
                vehicle.setType(input.type());
            }
            if (input.studentId() != null) {
                var student = studentRepository.findById(input.studentId());
                student.ifPresent(vehicle::setStudent);
            }
            return this.vehicleRepository.save(vehicle);
        });
    }

    @Transactional
    public Vehicle upsert(UpsertVehicleInput input) {
        Vehicle vehicle;
        if (input.id() != null) {
            vehicle = this.vehicleRepository.findById(input.id()).orElseGet(() -> Vehicle.builder().build());
        } else {
            vehicle = Vehicle.builder().build();
        }
        vehicle.setType(input.type());
        if (input.studentId() != null) {
            var student = studentRepository.findById(input.studentId());
            student.ifPresent(vehicle::setStudent);
        }
        return this.vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll(Integer limit) {
        var stream = this.vehicleRepository.findAll().stream();
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream.toList();
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
