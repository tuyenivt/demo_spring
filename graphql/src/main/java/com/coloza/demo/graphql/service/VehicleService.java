package com.coloza.demo.graphql.service;

import com.coloza.demo.graphql.model.entity.Vehicle;
import com.coloza.demo.graphql.model.repository.StudentRepository;
import com.coloza.demo.graphql.model.repository.VehicleRepository;
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
    public Vehicle create(String type, int studentId) {
        var vehicle = new Vehicle();
        vehicle.setType(type);
        var student = studentRepository.findById(studentId);
        student.ifPresent(vehicle::setStudent);
        return this.vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll(int limit) {
        return this.vehicleRepository.findAll().stream().limit(limit).toList();
    }
}
