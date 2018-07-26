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
        System.out.println("testServe - before - factor 1");
        service.serve(1);
        System.out.println("testServe - after - factor 1");
        System.out.println("testServe - before - factor 2");
        service.serve(2);
        System.out.println("testServe - after - factor 2");
    }

}
