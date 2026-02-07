package com.example.aop.aspect;

import com.example.aop.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for DemoAspect.
 * Verifies that all 5 advice types and pointcut composition work correctly.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class DemoAspectTest {

    @Autowired
    private AccountService accountService;

    @Test
    void beforeAdvice_fires(CapturedOutput output) {
        accountService.addAccount();

        assertThat(output).contains("[DEMO @Before] About to execute AccountDao.add()");
    }

    @Test
    void afterReturningAdvice_fires(CapturedOutput output) {
        accountService.findAccounts(List.of(1, 2));

        assertThat(output).contains("[DEMO @AfterReturning] AccountDao.find() returned:");
    }

    @Test
    void afterThrowingAdvice_fires(CapturedOutput output) {
        assertThatThrownBy(() -> accountService.deleteAccount(1))
                .isInstanceOf(RuntimeException.class);

        assertThat(output).contains("[DEMO @AfterThrowing] AccountDao.delete() threw exception:");
    }

    @Test
    void afterAdvice_fires(CapturedOutput output) {
        accountService.findAccounts(List.of(1));

        assertThat(output).contains("[DEMO @After] Cleanup after AccountDao.find(..)");
    }

    @Test
    void aroundAdvice_fires(CapturedOutput output) {
        assertThatThrownBy(() -> accountService.findAccountsOrExceptionIfNotFound(List.of()))
                .isInstanceOf(RuntimeException.class);

        assertThat(output).contains("[DEMO @Around] Before proceeding to AccountDao.findOrExceptionIfNotFound(..)");
    }

    @Test
    void composedPointcut_fires(CapturedOutput output) throws InterruptedException {
        accountService.serve(1);

        assertThat(output).contains("[DEMO POINTCUT &&] Matched @Service + @ExecutionLogging");
    }
}
