package com.example.multiple.databases.oldDemo.repository;

import com.example.multiple.databases.oldDemo.entity.OldProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OldProductRepository extends JpaRepository<OldProduct, Long> {
    int countByUpdatedAtGreaterThan(LocalDateTime updatedAt);

    List<OldProduct> findByUpdatedAtGreaterThanOrderByUpdatedAt(LocalDateTime updatedAt, Pageable pageable);
}
