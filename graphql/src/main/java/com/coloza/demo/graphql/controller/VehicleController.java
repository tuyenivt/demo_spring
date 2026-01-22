package com.coloza.demo.graphql.controller;

import com.coloza.demo.graphql.entity.Vehicle;
import com.coloza.demo.graphql.dto.CreateVehicleInput;
import com.coloza.demo.graphql.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @QueryMapping
    public List<Vehicle> vehicles(@Argument Integer limit) {
        return this.service.findAll(limit);
    }

    @MutationMapping
    public Vehicle createVehicle(@Argument @Valid CreateVehicleInput input) {
        return this.service.create(input);
    }
}
