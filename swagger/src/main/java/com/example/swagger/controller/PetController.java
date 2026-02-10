package com.example.swagger.controller;

import com.example.swagger.dto.CreatePetRequest;
import com.example.swagger.dto.ErrorResponse;
import com.example.swagger.dto.PetResponse;
import io.swagger.petstore.api.PetApi;
import io.swagger.petstore.model.Pet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pet", description = "Pet management endpoints")
public class PetController {

    private final PetApi petApi;

    public PetController(PetApi petApi) {
        this.petApi = petApi;
    }

    @GetMapping("/{petId}")
    @Operation(summary = "Get pet by ID")
    @ApiResponse(responseCode = "200", description = "Pet found", content = @Content(schema = @Schema(implementation = PetResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pet not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PetResponse getPetById(@PathVariable @Positive(message = "petId must be positive") Long petId) {
        return toResponse(petApi.getPetById(petId));
    }

    @GetMapping
    @Operation(summary = "Find pets by status")
    @ApiResponse(responseCode = "200", description = "Pets found", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PetResponse.class))))
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<PetResponse> findPetsByStatus(
            @RequestParam(defaultValue = "available")
            List<@Pattern(regexp = "available|pending|sold", message = "status must be available, pending, or sold") String> status
    ) {
        return petApi.findPetsByStatus(status).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a pet")
    @ApiResponse(responseCode = "201", description = "Pet created", content = @Content(schema = @Schema(implementation = PetResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PetResponse createPet(@Valid @RequestBody CreatePetRequest request) {
        var pet = new Pet().name(request.name()).status(Pet.StatusEnum.fromValue(request.status()));
        petApi.addPet(pet);
        return toResponse(pet);
    }

    private PetResponse toResponse(Pet pet) {
        return new PetResponse(pet.getId(), pet.getName(), pet.getStatus() == null ? null : pet.getStatus().getValue());
    }
}
