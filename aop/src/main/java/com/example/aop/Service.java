package com.example.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.aop.aspect.LogExecutionTime;
import com.example.aop.dao.AccountDao;

@Component
public class Service {
    
    @Autowired
    private AccountDao accountDao;

    @LogExecutionTime
    public void serve(int factor) throws InterruptedException {
        Thread.sleep(factor * 1000);
    }
    
    public void addAccount() {
        accountDao.add();
    }
}
