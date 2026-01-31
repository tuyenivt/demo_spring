package com.example.monitor.config;

import com.example.monitor.entity.Customer;
import com.example.monitor.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner seedData(CustomerRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Customer("1", "Alice"),
                        new Customer("2", "Bob"),
                        new Customer("3", "Charlie"),
                        new Customer("4", "Diana"),
                        new Customer("5", "Eve")
                ));
            }
        };
    }
}
