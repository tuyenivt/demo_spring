package com.example.versioning.dto;

import com.fasterxml.jackson.annotation.JsonView;

public record ApiResponse<T>(
        @JsonView(Views.V1.class)
        T data,
        @JsonView(Views.V1.class)
        ApiMeta meta
) {
}
