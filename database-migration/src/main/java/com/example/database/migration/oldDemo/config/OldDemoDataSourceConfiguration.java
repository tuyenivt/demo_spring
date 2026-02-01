package com.example.database.migration.oldDemo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "oldDemoEntityManagerFactory",
        transactionManagerRef = "oldDemoTransactionManager",
        basePackages = {"com.example.database.migration.oldDemo.repository"}
)
public class OldDemoDataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "oldDemo.datasource")
    public DataSource oldDemoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "oldDemoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean oldDemoEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("oldDemoDataSource") DataSource dataSource
    ) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        return builder
                .dataSource(dataSource)
                .packages("com.example.database.migration.oldDemo.entity")
                .persistenceUnit("oldDemo")
                .properties(properties)
                .build();
    }

    @Bean(name = "oldDemoTransactionManager")
    public PlatformTransactionManager oldDemoTransactionManager(
            @Qualifier("oldDemoEntityManagerFactory") EntityManagerFactory oldDemoEntityManagerFactory
    ) {
        return new JpaTransactionManager(oldDemoEntityManagerFactory);
    }
}
