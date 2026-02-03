package com.example.database.replication.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.database.replication.repository.write",
        entityManagerFactoryRef = "writerEntityManagerFactory",
        transactionManagerRef = "writerTransactionManager"
)
public class WriterDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.writer")
    public DataSource writerDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean writerEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("writerDataSource") DataSource dataSource
    ) {
        var properties = new HashMap<String, String>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        return builder
                .dataSource(dataSource)
                .packages("com.example.database.replication.entity")
                .persistenceUnit("writer")
                .properties(properties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager writerTransactionManager(
            @Qualifier("writerEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public SpringLiquibase liquibase(@Qualifier("writerDataSource") DataSource dataSource) {
        var liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setDataSource(dataSource);
        return liquibase;
    }
}
