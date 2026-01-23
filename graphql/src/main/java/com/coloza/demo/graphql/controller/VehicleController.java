package com.coloza.demo.graphql.controller;

import com.coloza.demo.graphql.dto.CreateVehicleInput;
import com.coloza.demo.graphql.dto.UpdateVehicleInput;
import com.coloza.demo.graphql.dto.UpsertVehicleInput;
import com.coloza.demo.graphql.dto.filter.VehicleFilter;
import com.coloza.demo.graphql.dto.pagination.Connection;
import com.coloza.demo.graphql.dto.pagination.ConnectionInput;
import com.coloza.demo.graphql.dto.pagination.PageInput;
import com.coloza.demo.graphql.dto.pagination.PageResult;
import com.coloza.demo.graphql.dto.sort.VehicleSort;
import com.coloza.demo.graphql.entity.Vehicle;
import com.coloza.demo.graphql.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

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
    public Vehicle createVehicle(@Argument @Valid CreateVehicleInput input) {
        return this.service.create(input);
    }

    @MutationMapping
    public List<Vehicle> createVehicles(@Argument @Valid List<CreateVehicleInput> inputs) {
        return this.service.createAll(inputs);
    }

    @MutationMapping
    public Optional<Vehicle> updateVehicle(@Argument @Valid UpdateVehicleInput input) {
        return this.service.update(input);
    }

    @MutationMapping
    public Vehicle upsertVehicle(@Argument @Valid UpsertVehicleInput input) {
        return this.service.upsert(input);
    }
}
