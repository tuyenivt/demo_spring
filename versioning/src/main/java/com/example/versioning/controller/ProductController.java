package com.example.versioning.controller;

import com.example.versioning.dto.ProductV1;
import com.example.versioning.dto.ProductV2;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates media type versioning using vendor-specific MIME types.
 * Client specifies version via Accept header with custom media type.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * V1 endpoint using custom media type.
     * Request with: Accept: application/vnd.company.v1+json
     */
    @Operation(summary = "Get product (v1 media type)", deprecated = true)
    @GetMapping(produces = "application/vnd.company.v1+json")
    public ProductV1 getProductV1() {
        return new ProductV1("Widget", 29.99);
    }

    /**
     * V2 endpoint using custom media type.
     * Request with: Accept: application/vnd.company.v2+json
     */
    @Operation(summary = "Get product (v2 media type)")
    @GetMapping(produces = "application/vnd.company.v2+json")
    public ProductV2 getProductV2() {
        return new ProductV2("Widget", 29.99, "Premium quality widget", "WIDGET-001");
    }
}
