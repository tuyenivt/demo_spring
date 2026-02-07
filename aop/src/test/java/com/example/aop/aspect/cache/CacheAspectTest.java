package com.example.aop.aspect.cache;

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
class CacheAspectTest {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private CacheAspect cacheAspect;

    @BeforeEach
    void setUp() {
        cacheAspect.clearCache();
    }

    @Test
    void slowFindById_firstCallIsCacheMiss(CapturedOutput output) {
        var result = accountDao.slowFindById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(output).contains("CACHE MISS:");
    }

    @Test
    void slowFindById_secondCallIsCacheHit(CapturedOutput output) {
        accountDao.slowFindById(1);
        accountDao.slowFindById(1);

        assertThat(output).contains("CACHE MISS:");
        assertThat(output).contains("CACHE HIT:");
    }

    @Test
    void slowFindById_differentArgsCauseSeparateMisses(CapturedOutput output) {
        accountDao.slowFindById(1);
        accountDao.slowFindById(2);

        var missCount = output.toString().split("CACHE MISS:").length - 1;
        assertThat(missCount).isEqualTo(2);
    }
}
