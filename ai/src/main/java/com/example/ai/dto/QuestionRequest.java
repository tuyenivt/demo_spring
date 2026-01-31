package com.example.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank(message = "Question cannot be blank")
        @Size(max = 2000, message = "Question cannot exceed 2000 characters")
        String question
) {
}
