package com.example.aop;

import com.example.aop.service.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

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

    @Test
    public void testFindAccounts() {
        logger.info("testFindAccounts - before");
        service.findAccounts(Arrays.asList(1, 2, 3, 4, 5));
        logger.info("testFindAccounts - after");
    }

    @Test
    public void testFindAccountsOrExceptionIfNotFound() {
        logger.info("testFindAccountsOrExceptionIfNotFound - before");
        try {
            service.findAccountsOrExceptionIfNotFound(Arrays.asList(1, 2, 3, 4, 5));
            Assertions.fail();
        } catch (Exception e) {
            logger.info("testFindAccountsOrExceptionIfNotFound - after with exception:" + e);
        }
    }

    @Test
    public void testAddAccount() {
        logger.info("testAddAccount - before");
        service.addAccount();
        logger.info("testAddAccount - after");
    }

    @Test
    public void testDeleteAccount() {
        logger.info("testDeleteAccount - before");
        try {
            service.deleteAccount(1);
            Assertions.fail();
        } catch (Exception e) {
            logger.info("testDeleteAccount - after with exception:" + e);
        }
    }

}
