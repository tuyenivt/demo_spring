package com.example.security;

import com.example.security.config.SecurityConfig;
import com.example.security.controller.HomeController;
import com.example.security.controller.LeadersController;
import com.example.security.controller.LoginController;
import com.example.security.controller.SystemsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({HomeController.class, LoginController.class, LeadersController.class, SystemsController.class})
@Import(SecurityConfig.class)
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/my-login/"));
    }

    @Test
    void loginPageRedirectsToLoginPageWithTrailingSlash() throws Exception {
        // Security config uses /my-login/ with trailing slash
        // This test verifies unauthenticated access redirects to log in
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/my-login/"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCanAccessHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotAccessLeaders() throws Exception {
        mockMvc.perform(get("/leaders/"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotAccessSystems() throws Exception {
        mockMvc.perform(get("/systems/"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanAccessLeaders() throws Exception {
        mockMvc.perform(get("/leaders/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCannotAccessSystems() throws Exception {
        mockMvc.perform(get("/systems/"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE", "ADMIN"})
    void adminCanAccessSystems() throws Exception {
        mockMvc.perform(get("/systems/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE", "MANAGER"})
    void managerWithEmployeeCanAccessBothHomeAndLeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/leaders/"))
                .andExpect(status().isOk());
    }

    @Test
    void accessDeniedPageIsAccessible() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void accessDeniedPageIsAccessibleForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isOk());
    }
}
