package com.coloza.demo.graphql.controller;

import com.coloza.demo.graphql.model.entity.Vehicle;
import com.coloza.demo.graphql.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @QueryMapping
    public List<Vehicle> getVehicles(int limit) {
        return this.service.findAll(limit);
    }

    @MutationMapping
    public Vehicle createVehicle(String type, int studentId) {
        return this.service.create(type, studentId);
    }
}
