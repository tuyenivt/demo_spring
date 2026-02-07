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
 * Tests for PerformanceMonitoringAspect.
 * Verifies that @MonitorPerformance annotation detects slow methods.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class PerformanceMonitoringAspectTest {

    @Autowired
    private AccountService accountService;

    @Test
    void monitorPerformance_logsSlowMethod(CapturedOutput output) throws InterruptedException {
        // serve() has @MonitorPerformance(thresholdMs = 500)
        // and sleeps for 1000ms, so it should trigger slow warning
        accountService.serve(1);

        assertThat(output)
                .contains("SLOW METHOD:")
                .contains("AccountService.serve(..)")
                .contains("threshold: 500ms");
    }
}
