package com.example.aop.aspect;

import com.example.aop.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ExecutionLoggingAspect.
 * Verifies that @ExecutionLogging annotation triggers detailed execution logging.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class ExecutionLoggingAspectTest {

    @Autowired
    private AccountService accountService;

    @Test
    void executionLogging_logsMethodExecution(CapturedOutput output) throws InterruptedException {
        accountService.serve(1);

        assertThat(output)
                .contains("Executing: AccountService.serve(..)")
                .contains("Completed: AccountService.serve(..) in")
                .contains("ms");
    }

    @Test
    void executionLogging_logsArguments(CapturedOutput output) throws InterruptedException {
        accountService.serve(2);

        assertThat(output).contains("with args: [2]");
    }
}
