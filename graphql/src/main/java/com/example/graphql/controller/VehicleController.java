package com.example.graphql.controller;

import com.example.graphql.dto.filter.VehicleFilter;
import com.example.graphql.dto.input.CreateVehicleInput;
import com.example.graphql.dto.input.UpdateVehicleInput;
import com.example.graphql.dto.input.UpsertVehicleInput;
import com.example.graphql.dto.pagination.Connection;
import com.example.graphql.dto.pagination.ConnectionInput;
import com.example.graphql.dto.pagination.PageInput;
import com.example.graphql.dto.pagination.PageResult;
import com.example.graphql.dto.sort.VehicleSort;
import com.example.graphql.entity.Student;
import com.example.graphql.entity.Vehicle;
import com.example.graphql.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @Deprecated
    @QueryMapping
    public List<Vehicle> vehicles(@Argument Integer limit) {
        return this.service.findAll(limit);
    }

    @QueryMapping
    public PageResult<Vehicle> vehiclesPage(
            @Argument PageInput page,
            @Argument VehicleFilter filter,
            @Argument VehicleSort sort) {
        return this.service.findPage(page, filter, sort);
    }

    @QueryMapping
    public Connection<Vehicle> vehiclesConnection(
            @Argument ConnectionInput connection,
            @Argument VehicleFilter filter,
            @Argument VehicleSort sort) {
        return this.service.findConnection(connection, filter, sort);
    }

    @MutationMapping
    public Vehicle createVehicle(@Argument CreateVehicleInput input) {
        return this.service.create(input);
    }

    @MutationMapping
    public List<Vehicle> createVehicles(@Argument List<CreateVehicleInput> inputs) {
        return this.service.createAll(inputs);
    }

    @MutationMapping
    public Vehicle updateVehicle(@Argument UpdateVehicleInput input) {
        return this.service.update(input);
    }

    @MutationMapping
    public Vehicle upsertVehicle(@Argument UpsertVehicleInput input) {
        return this.service.upsert(input);
    }

    @BatchMapping
    public Map<Vehicle, Student> student(List<Vehicle> vehicles) {
        return this.service.findStudentsForVehicles(vehicles);
    }
}
