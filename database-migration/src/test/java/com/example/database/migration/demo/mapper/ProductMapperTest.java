package com.example.database.migration.demo.mapper;

import com.example.database.migration.oldDemo.entity.OldProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    void shouldMapAllFieldsCorrectly() {
        // Given
        var now = LocalDateTime.now();
        var oldProduct = OldProduct.builder()
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("19.99"))
                .quality(100L)
                .dateOfManufacture(now)
                .updatedAt(now)
                .build();

        // When
        var product = productMapper.from(oldProduct, -7);

        // Then
        assertThat(product.getProductId()).isEqualTo(1L);
        assertThat(product.getProductName()).isEqualTo("Test Product");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(product.getInStock()).isEqualTo(100L);
        assertThat(product.getDateOfManufacture()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now.plusHours(-7));
        assertThat(product.getVendor()).isEqualTo("ABC");
    }

    @Test
    void shouldHandleNullUpdatedAt() {
        // Given
        var oldProduct = OldProduct.builder()
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("19.99"))
                .quality(100L)
                .dateOfManufacture(LocalDateTime.now())
                .updatedAt(null)
                .build();

        // When
        var product = productMapper.from(oldProduct, -7);

        // Then
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    void shouldDefaultQualityToZeroWhenNull() {
        // Given
        var oldProduct = OldProduct.builder()
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("19.99"))
                .quality(null)
                .dateOfManufacture(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        var product = productMapper.from(oldProduct, -7);

        // Then
        assertThat(product.getInStock()).isZero();
    }

    @Test
    void shouldApplyCustomTimezoneOffset() {
        // Given
        var now = LocalDateTime.now();
        var oldProduct = OldProduct.builder()
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("19.99"))
                .quality(100L)
                .dateOfManufacture(now)
                .updatedAt(now)
                .build();

        // When
        var product = productMapper.from(oldProduct, -5);

        // Then
        assertThat(product.getUpdatedAt()).isEqualTo(now.plusHours(-5));
    }

    @Test
    void shouldAlwaysSetVendorToABC() {
        // Given
        var oldProduct = OldProduct.builder()
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("19.99"))
                .quality(100L)
                .dateOfManufacture(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        var product = productMapper.from(oldProduct, -7);

        // Then
        assertThat(product.getVendor()).isEqualTo("ABC");
    }
}
