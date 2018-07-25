package com.example.aop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MainApplicationTests {

    @Autowired
    private Service service;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testServe() throws InterruptedException {
        System.out.println("testServe - before");
        service.serve();
        System.out.println("testServe - after");
    }

}
