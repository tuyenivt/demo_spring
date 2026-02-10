package com.example.modulith.inventory.infrastructure.web;

import com.example.modulith.inventory.*;
import com.example.modulith.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management API")
class InventoryController {

    private final InventoryFacade inventoryFacade;

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock", description = "Reserves stock quantity for a product")
    public ResponseEntity<ApiResponse<Void>> reserveStock(@Valid @RequestBody ReserveStockCommand command) {
        inventoryFacade.reserveStock(command);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Stock reserved successfully").build());
    }

    @GetMapping("/check/{sku}")
    @Operation(summary = "Check stock availability", description = "Checks if product has sufficient stock")
    public ResponseEntity<ApiResponse<Boolean>> checkStock(@PathVariable String sku, @RequestParam int quantity) {
        var inStock = inventoryFacade.isInStock(sku, quantity);
        return ResponseEntity.ok(ApiResponse.success(inStock));
    }

    @GetMapping("/products")
    @Operation(summary = "List products", description = "Returns paginated products")
    public ApiResponse<Page<ProductResponse>> listProducts(Pageable pageable) {
        return ApiResponse.success(inventoryFacade.listProducts(pageable));
    }

    @GetMapping("/products/{sku}")
    @Operation(summary = "Get product by SKU", description = "Fetches a product by SKU")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String sku) {
        return ApiResponse.success(inventoryFacade.getProductBySku(sku));
    }
}
