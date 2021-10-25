package com.example.multiple.databases.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:100}")
    private int size;
}
