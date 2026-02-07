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
 * Demonstrates the Spring AOP self-invocation proxy limitation.
 * When a bean calls its own method internally, the call bypasses the proxy
 * and aspects do not fire.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SelfInvocationTest {

    @Autowired
    private AccountService accountService;

    @Test
    void directCall_aspectFires(CapturedOutput output) throws InterruptedException {
        // Calling serve() through the proxy — aspect DOES fire
        accountService.serve(1);

        assertThat(output).contains("Executing: AccountService.serve(..)");
    }

    @Test
    void selfInvocation_aspectDoesNotFire(CapturedOutput output) throws InterruptedException {
        // processBatch() internally calls serve() — aspect does NOT fire
        // because the internal call bypasses the AOP proxy
        accountService.processBatch(1);

        // The @ExecutionLogging around advice should NOT appear for the inner serve() call
        assertThat(output).contains("processBatch - calling serve() internally");
        assertThat(output).doesNotContain("Executing: AccountService.serve(..)");
    }
}
