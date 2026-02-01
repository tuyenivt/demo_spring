package com.example.database.migration.demo.task;

import com.example.database.migration.config.AppConfig;
import com.example.database.migration.demo.mapper.ProductMapper;
import com.example.database.migration.demo.repository.ProductRepository;
import com.example.database.migration.oldDemo.entity.OldProduct;
import com.example.database.migration.oldDemo.repository.OldProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTasks {

    private final AppConfig appConfig;

    private final ProductRepository productRepository;
    private final OldProductRepository oldProductRepository;

    private static final String UPDATED_AT_FILE = "updatedAt_Product.txt";
    private static final String FIRST_TIME = "1970-01-01T00:00:00";

    @Scheduled(fixedRateString = "${scheduled.fixedRate:60000}")
    public void migrate() throws IOException {
        log.info("migrate trigger...");
        // check last update file
        Path updatedAtFile = new File(UPDATED_AT_FILE).toPath();
        if (!Files.exists(updatedAtFile)) {
            log.info("migrate {} file not exist, creating a new file", UPDATED_AT_FILE);
            Files.write(updatedAtFile, Arrays.asList(FIRST_TIME), StandardCharsets.UTF_8);
        }
        LocalDateTime lastUpdatedAt = LocalDateTime.parse(Files.readAllLines(updatedAtFile, StandardCharsets.UTF_8).get(0), DateTimeFormatter.ISO_DATE_TIME);
        log.info("migrate lastUpdatedAt = {}", lastUpdatedAt);

        int count = oldProductRepository.countByUpdatedAtGreaterThan(lastUpdatedAt);
        log.info("migrate new change count = {}", count);
        if (count <= 0) {
            log.info("migrate skip migration");
            return;
        }

        LocalDateTime start = LocalDateTime.now();
        log.info("migrate migrating data to target datasource...");

        int size = appConfig.getSize();
        int page = 0;
        while (page * size < count) {
            log.info("migrate query data from source datasource, offset [{}, {}]", page * size + 1, (page + 1) * size);
            List<OldProduct> oldProductRecords = oldProductRepository.findByUpdatedAtGreaterThanOrderByUpdatedAt(lastUpdatedAt, PageRequest.of(page, size));
            productRepository.saveAll(oldProductRecords.parallelStream().map(ProductMapper.INSTANCE::from).collect(Collectors.toList()));
            page++;

            Optional<LocalDateTime> lastUpdatedAtOptional = oldProductRecords.parallelStream().map(OldProduct::getUpdatedAt).max(LocalDateTime::compareTo);
            if (lastUpdatedAtOptional.isPresent()) {
                log.info("migrate updating {}", UPDATED_AT_FILE);
                Files.write(updatedAtFile, Arrays.asList(lastUpdatedAtOptional.get().toString()), StandardCharsets.UTF_8);
            }
        }

        log.info("migrate data migrated, took {}", Duration.between(start, LocalDateTime.now()));
    }
}
