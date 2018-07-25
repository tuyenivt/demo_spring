package com.example.aop;

import org.springframework.stereotype.Component;

import com.example.aop.aspect.LogExecutionTime;

@Component
public class Service {

    @LogExecutionTime
    public void serve() throws InterruptedException {
        Thread.sleep(2000);
    }
}
