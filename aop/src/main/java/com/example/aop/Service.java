package com.example.aop;

import org.springframework.stereotype.Component;

import com.example.aop.aspect.LogExecutionTime;

@Component
public class Service {

    @LogExecutionTime
    public void serve(int factor) throws InterruptedException {
        Thread.sleep(factor * 1000);
    }
}
