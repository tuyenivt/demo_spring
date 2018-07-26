package com.example.aop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MainApplicationTests {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Service service;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testServe() throws InterruptedException {
    	logger.info("testServe - before - factor 1");
        service.serve(1);
        logger.info("testServe - after - factor 1");
        logger.info("testServe - before - factor 2");
        service.serve(2);
        logger.info("testServe - after - factor 2");
    }

}
