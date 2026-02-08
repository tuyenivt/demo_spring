package com.example.database.migration.demo.service;

import com.example.database.migration.demo.entity.MigrationState;
import com.example.database.migration.demo.entity.Product;
import com.example.database.migration.demo.repository.MigrationStateRepository;
import com.example.database.migration.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMigrationService {

    private final ProductRepository productRepository;
    private final MigrationStateRepository migrationStateRepository;

    @Transactional("demoTransactionManager")
    public void saveBatchAndUpdateState(List<Product> products, MigrationState state, LocalDateTime newLastUpdatedAt) {
        productRepository.saveAll(products);
        state.setLastUpdatedAt(newLastUpdatedAt);
        state.setModifiedAt(LocalDateTime.now());
        migrationStateRepository.save(state);
    }
}
