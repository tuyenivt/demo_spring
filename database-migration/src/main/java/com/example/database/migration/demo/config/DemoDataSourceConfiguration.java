package com.example.database.migration.demo.config;

import jakarta.persistence.EntityManagerFactory;
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
        entityManagerFactoryRef = "demoEntityManagerFactory",
        transactionManagerRef = "demoTransactionManager",
        basePackages = {"com.example.database.migration.demo.repository"}
)
public class DemoDataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "demo.datasource")
    public DataSource demoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "demoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean demoEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("demoDataSource") DataSource dataSource
    ) {
        var properties = new HashMap<String, String>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        return builder
                .dataSource(dataSource)
                .packages("com.example.database.migration.demo.entity")
                .persistenceUnit("demo")
                .properties(properties)
                .build();
    }

    @Bean(name = "demoTransactionManager")
    public PlatformTransactionManager demoTransactionManager(
            @Qualifier("demoEntityManagerFactory") EntityManagerFactory demoEntityManagerFactory
    ) {
        return new JpaTransactionManager(demoEntityManagerFactory);
    }
}
