package com.example.caching.service;

import com.example.caching.entity.Product;
import com.example.caching.enums.Category;
import com.example.caching.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .productId(1L)
                .productName("Test Product")
                .category(Category.PRODUCT)
                .price(new BigDecimal("29.99"))
                .inStock(100L)
                .dateOfManufacture(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .vendor("Test Vendor")
                .build();
    }

    @Test
    void findById_shouldReturnProduct_whenProductExists() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(1L);
        assertThat(result.get().getProductName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldReturnEmpty_whenProductNotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void findById_shouldFallbackToDatabase_whenDataAccessExceptionOccurs() {
        // Given
        when(productRepository.findById(1L))
                .thenThrow(new DataAccessException("Redis connection failed") {
                })
                .thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(1L);
        verify(productRepository, times(2)).findById(1L);
    }

    @Test
    void findByProductNameOrderByUpdatedAtDesc_shouldReturnProducts() {
        // Given
        List<Product> products = List.of(testProduct);
        when(productRepository.findByProductName(eq("Test"), any(Sort.class)))
                .thenReturn(products);

        // When
        List<Product> result = productService.findByProductNameOrderByUpdatedAtDesc("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).findByProductName(eq("Test"), any(Sort.class));
    }

    @Test
    void findByProductNameOrderByUpdatedAtDesc_shouldFallbackToDatabase_whenDataAccessExceptionOccurs() {
        // Given
        List<Product> products = List.of(testProduct);
        when(productRepository.findByProductName(eq("Test"), any(Sort.class)))
                .thenThrow(new DataAccessException("Redis connection failed") {
                })
                .thenReturn(products);

        // When
        List<Product> result = productService.findByProductNameOrderByUpdatedAtDesc("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Test Product");
        verify(productRepository, times(2)).findByProductName(eq("Test"), any(Sort.class));
    }

    @Test
    void save_shouldSaveProduct() {
        // Given
        Product newProduct = Product.builder()
                .productName("New Product")
                .category(Category.PRODUCT)
                .price(new BigDecimal("49.99"))
                .inStock(50L)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.save(newProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void save_shouldUpdateExistingProduct() {
        // Given
        testProduct.setPrice(new BigDecimal("39.99"));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        // When
        Product result = productService.save(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("39.99"));
        verify(productRepository, times(1)).save(testProduct);
    }
}
