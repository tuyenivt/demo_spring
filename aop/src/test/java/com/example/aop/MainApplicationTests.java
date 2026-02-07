package com.example.aop;

import com.example.aop.service.AccountService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class MainApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountService accountService;

    @Test
    void contextLoads() {
    }

    @Test
    void testServe() throws InterruptedException {
        logger.info("testServe - before - factor 1");
        accountService.serve(1);
        logger.info("testServe - after - factor 1");
    }

    @Test
    void testFindAccounts() {
        logger.info("testFindAccounts - before");
        accountService.findAccounts(List.of(1, 2, 3, 4, 5));
        logger.info("testFindAccounts - after");
    }

    @Test
    void testFindAccountsOrExceptionIfNotFound() {
        logger.info("testFindAccountsOrExceptionIfNotFound - before");
        try {
            accountService.findAccountsOrExceptionIfNotFound(List.of(1, 2, 3, 4, 5));
            fail();
        } catch (Exception e) {
            logger.error("testFindAccountsOrExceptionIfNotFound - after with exception:", e);
        }
    }

    @Test
    void testAddAccount() {
        logger.info("testAddAccount - before");
        accountService.addAccount();
        logger.info("testAddAccount - after");
    }

    @Test
    void testDeleteAccount() {
        logger.info("testDeleteAccount - before");
        try {
            accountService.deleteAccount(1);
            fail();
        } catch (Exception e) {
            logger.error("testDeleteAccount - after with exception:", e);
        }
    }
}
