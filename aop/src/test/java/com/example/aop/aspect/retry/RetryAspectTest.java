package com.example.aop.aspect.retry;

import com.example.aop.dao.AccountDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class RetryAspectTest {

    @Autowired
    private AccountDao accountDao;

    @BeforeEach
    void setUp() {
        accountDao.resetFetchCounter();
    }

    @Test
    void fetchWithRetry_succeedsOnThirdAttempt(CapturedOutput output) {
        var result = accountDao.fetchWithRetry(42);

        assertThat(result.getId()).isEqualTo(42);
        assertThat(result.getName()).isEqualTo("Fetched");
        assertThat(output).contains("Attempt 1/3 failed");
        assertThat(output).contains("Retry attempt 2/3");
        assertThat(output).contains("Attempt 2/3 failed");
        assertThat(output).contains("Retry attempt 3/3");
    }
}
