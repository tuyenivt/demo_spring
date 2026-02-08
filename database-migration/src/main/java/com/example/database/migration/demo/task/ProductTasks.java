package com.example.database.migration.demo.task;

import com.example.database.migration.config.AppConfig;
import com.example.database.migration.demo.entity.MigrationState;
import com.example.database.migration.demo.mapper.ProductMapper;
import com.example.database.migration.demo.repository.MigrationStateRepository;
import com.example.database.migration.demo.service.ProductMigrationService;
import com.example.database.migration.health.MigrationHealthIndicator;
import com.example.database.migration.oldDemo.entity.OldProduct;
import com.example.database.migration.oldDemo.service.OldProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTasks {

    private final AppConfig appConfig;
    private final ProductMapper productMapper;
    private final MigrationStateRepository migrationStateRepository;
    private final ProductMigrationService productMigrationService;
    private final OldProductService oldProductService;
    private final MigrationHealthIndicator healthIndicator;

    private static final String ENTITY_NAME = "Product";
    private static final LocalDateTime FIRST_TIME = LocalDateTime.parse("1970-01-01T00:00:00");
    private static final int MAX_RETRY_COUNT = 3;

    private int consecutiveFailures = 0;

    @Scheduled(fixedRateString = "${scheduled.fixedRate:60000}")
    @SchedulerLock(name = "productMigration", lockAtLeastFor = "10s", lockAtMostFor = "5m")
    public void migrate() {
        healthIndicator.setMigrationRunning(true);
        int totalProcessed;
        try {
            log.info("migrate trigger...");
            // Get last sync timestamp from database
            var state = migrationStateRepository.findById(ENTITY_NAME)
                    .orElseGet(() -> {
                        log.info("migrate: no existing state found, creating initial state");
                        var newState = MigrationState.builder()
                                .entityName(ENTITY_NAME)
                                .lastUpdatedAt(FIRST_TIME)
                                .updatedBy("system")
                                .createdAt(LocalDateTime.now())
                                .build();
                        return migrationStateRepository.save(newState);
                    });
            var lastUpdatedAt = state.getLastUpdatedAt();
            log.info("migrate lastUpdatedAt = {}", lastUpdatedAt);

            var count = oldProductService.countByUpdatedAtGreaterThan(lastUpdatedAt);
            log.info("migrate new change count = {}", count);
            if (count <= 0) {
                log.info("migrate skip migration");
                consecutiveFailures = 0; // Reset on successful check
                healthIndicator.setLastBatchSize(0);
                return;
            }
            totalProcessed = count;

            var start = LocalDateTime.now();
            log.info("migrate migrating data to target datasource...");

            var size = appConfig.getSize();
            var page = 0;
            while (page * size < count) {
                log.info("migrate query data from source datasource, offset [{}, {}]", page * size + 1, (page + 1) * size);
                var oldProductRecords = oldProductService.findByUpdatedAtGreaterThan(lastUpdatedAt, PageRequest.of(page, size));

                var products = oldProductRecords.stream()
                        .map(record -> productMapper.from(record, appConfig.getTimezoneOffsetHours()))
                        .toList();

                var lastUpdatedAtOptional = oldProductRecords.stream()
                        .map(OldProduct::getUpdatedAt)
                        .filter(java.util.Objects::nonNull)
                        .max(LocalDateTime::compareTo);

                if (lastUpdatedAtOptional.isPresent()) {
                    log.info("migrate updating migration state");
                    productMigrationService.saveBatchAndUpdateState(products, state, lastUpdatedAtOptional.get());
                }

                page++;
            }

            log.info("migrate data migrated, took {}", Duration.between(start, LocalDateTime.now()));
            consecutiveFailures = 0; // Reset on success
            healthIndicator.setLastBatchSize(totalProcessed);
        } catch (Exception e) {
            consecutiveFailures++;
            log.error("migrate failed (consecutive failures: {}): {}", consecutiveFailures, e.getMessage(), e);

            if (consecutiveFailures >= MAX_RETRY_COUNT) {
                log.error("CRITICAL: Migration has failed {} consecutive times - manual intervention may be required", consecutiveFailures);
            }
        } finally {
            healthIndicator.setMigrationRunning(false);
        }
    }
}
