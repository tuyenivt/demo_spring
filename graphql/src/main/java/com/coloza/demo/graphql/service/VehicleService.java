package com.coloza.demo.graphql.service;

import com.coloza.demo.graphql.entity.Vehicle;
import com.coloza.demo.graphql.dto.CreateVehicleInput;
import com.coloza.demo.graphql.repository.StudentRepository;
import com.coloza.demo.graphql.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public Vehicle create(CreateVehicleInput input) {
        var vehicle = new Vehicle();
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
}
