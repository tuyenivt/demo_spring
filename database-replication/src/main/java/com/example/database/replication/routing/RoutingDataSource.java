package com.example.database.replication.routing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routing datasource that determines the target datasource based on the current context.
 * Uses {@link DataSourceContextHolder} to get the routing key.
 *
 * <p>By default, routes to READER datasource. When {@link UseWriter} annotation
 * is present on a method, the aspect switches the context to WRITER.
 */
@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        var dataSourceType = DataSourceContextHolder.getCurrentDataSource();
        log.debug("Routing to {} datasource", dataSourceType);
        return dataSourceType;
    }
}
