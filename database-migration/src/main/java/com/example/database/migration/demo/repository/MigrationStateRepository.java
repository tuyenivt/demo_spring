package com.example.database.migration.demo.repository;

import com.example.database.migration.demo.entity.MigrationState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MigrationStateRepository extends JpaRepository<MigrationState, String> {
}
