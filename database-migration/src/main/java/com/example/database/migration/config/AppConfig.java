package com.example.database.migration.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:100}")
    private int size;

    @Value("${migration.timezone-offset-hours:-7}")
    private int timezoneOffsetHours;
}
