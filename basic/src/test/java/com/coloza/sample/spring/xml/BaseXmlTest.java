package com.coloza.sample.spring.xml;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class BaseXmlTest {

    protected ClassPathXmlApplicationContext context;

    @Before
    public void setup() {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    @After
    public void teardown() {
        context.close();
    }
}
