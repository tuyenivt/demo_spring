package com.coloza.sample.spring.config;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.coloza.sample.spring.SportConfig;

public abstract class BaseConfigTest {

    protected AnnotationConfigApplicationContext context;

    @Before
    public void setup() {
        context = new AnnotationConfigApplicationContext(SportConfig.class);
    }

    @After
    public void teardown() {
        context.close();
    }
}
