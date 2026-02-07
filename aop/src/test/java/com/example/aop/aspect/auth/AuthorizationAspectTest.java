package com.example.aop.aspect.auth;

import com.example.aop.dao.AccountDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class AuthorizationAspectTest {

    @Autowired
    private AccountDao accountDao;

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    void delete_withAdminRole_isAllowed(CapturedOutput output) {
        SecurityContext.setRole("ADMIN");

        // delete still throws RuntimeException from the DAO itself,
        // but the authorization check passes
        assertThatThrownBy(() -> accountDao.delete(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not allowed delete any account");

        assertThat(output).contains("ACCESS GRANTED:");
    }

    @Test
    void delete_withoutRole_isDenied(CapturedOutput output) {
        // no role set

        assertThatThrownBy(() -> accountDao.delete(1))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("required role 'ADMIN'");

        assertThat(output).contains("ACCESS DENIED:");
    }

    @Test
    void delete_withWrongRole_isDenied(CapturedOutput output) {
        SecurityContext.setRole("USER");

        assertThatThrownBy(() -> accountDao.delete(1))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(output).contains("ACCESS DENIED:");
    }
}
