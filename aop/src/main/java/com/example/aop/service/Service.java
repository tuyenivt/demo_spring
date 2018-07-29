package com.example.aop.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.aop.aspect.LogExecutionTime;
import com.example.aop.dao.AccountDao;
import com.example.aop.entity.Account;

@Component
public class Service {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountDao accountDao;

    @LogExecutionTime
    public void serve(int factor) throws InterruptedException {
        Thread.sleep(factor * 1000);
    }

    public List<Account> findAccounts(List<Integer> ids) {
        logger.info("findAccounts - executing...");
        List<Account> found = accountDao.find(ids);
        logger.info("findAccounts - returned result is " + found);
        return found;
    }

    public List<Account> findAccountsOrExceptionIfNotFound(List<Integer> ids) {
        logger.info("findAccountsOrExceptionIfNotFound - executing...");
        List<Account> found = accountDao.findOrExceptionIfNotFound(ids);
        logger.info("findAccountsOrExceptionIfNotFound - returned result is " + found);
        return found;
    }

    public void addAccount() {
        accountDao.add();
    }

    public void deleteAccount(int id) {
        accountDao.delete(id);
    }
}
