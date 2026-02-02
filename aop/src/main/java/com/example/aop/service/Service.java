package com.example.aop.service;

import com.example.aop.aspect.LogExecutionTime;
import com.example.aop.dao.AccountDao;
import com.example.aop.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Service {

    private final AccountDao accountDao;

    @LogExecutionTime
    public void serve(int factor) throws InterruptedException {
        Thread.sleep(factor * 1000L);
    }

    public List<Account> findAccounts(List<Integer> ids) {
        log.info("findAccounts - executing...");
        var found = accountDao.find(ids);
        log.info("findAccounts - returned result is {}", found);
        return found;
    }

    public List<Account> findAccountsOrExceptionIfNotFound(List<Integer> ids) {
        log.info("findAccountsOrExceptionIfNotFound - executing...");
        var found = accountDao.findOrExceptionIfNotFound(ids);
        log.info("findAccountsOrExceptionIfNotFound - returned result is {}", found);
        return found;
    }

    public void addAccount() {
        accountDao.add();
    }

    public void deleteAccount(int id) {
        accountDao.delete(id);
    }
}
