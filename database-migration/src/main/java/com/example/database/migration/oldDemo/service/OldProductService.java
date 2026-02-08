package com.example.database.migration.oldDemo.service;

import com.example.database.migration.oldDemo.entity.OldProduct;
import com.example.database.migration.oldDemo.repository.OldProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OldProductService {

    private final OldProductRepository oldProductRepository;

    @Transactional(value = "oldDemoTransactionManager", readOnly = true)
    public int countByUpdatedAtGreaterThan(LocalDateTime updatedAt) {
        return oldProductRepository.countByUpdatedAtGreaterThan(updatedAt);
    }

    @Transactional(value = "oldDemoTransactionManager", readOnly = true)
    public List<OldProduct> findByUpdatedAtGreaterThan(LocalDateTime updatedAt, Pageable pageable) {
        return oldProductRepository.findByUpdatedAtGreaterThanOrderByUpdatedAt(updatedAt, pageable);
    }
}
