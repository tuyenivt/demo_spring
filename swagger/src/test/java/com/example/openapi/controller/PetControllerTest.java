package com.example.openapi.controller;

import com.example.openapi.exception.GlobalExceptionHandler;
import com.example.openapi.petstore.api.PetApi;
import com.example.openapi.petstore.model.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import(GlobalExceptionHandler.class)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetApi petApi;

    @Test
    void getPetByIdReturns200() throws Exception {
        var pet = new Pet().id(1L).name("Buddy").status(Pet.StatusEnum.AVAILABLE);
        when(petApi.getPetById(1L)).thenReturn(pet);

        mockMvc.perform(get("/api/pets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.status").value("available"));
    }

    @Test
    void findPetsByStatusReturns200() throws Exception {
        var pet = new Pet().id(2L).name("Milo").status(Pet.StatusEnum.PENDING);
        when(petApi.findPetsByStatus(List.of("pending"))).thenReturn(List.of(pet));

        mockMvc.perform(get("/api/pets").queryParam("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Milo"))
                .andExpect(jsonPath("$[0].status").value("pending"));
    }

    @Test
    void createPetReturns201() throws Exception {
        mockMvc.perform(post("/api/pets")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Buddy",
                                  "status": "available"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.status").value("available"));

        verify(petApi).addPet(org.mockito.ArgumentMatchers.any(Pet.class));
    }

    @Test
    void createPetWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(post("/api/pets")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Buddy",
                                  "status": "broken"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
