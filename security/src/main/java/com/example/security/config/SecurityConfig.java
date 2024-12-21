package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean // configure users
    public UserDetailsService userDetailsService() {
        var users = User.withDefaultPasswordEncoder(); // for demo only
        return new InMemoryUserDetailsManager(
                users.username("john").password("123").roles("EMPLOYEE").build(),
                users.username("peter").password("123").roles("EMPLOYEE", "MANAGER").build(),
                users.username("frank").password("123").roles("EMPLOYEE", "ADMIN").build()
        );
    }

    @Bean // configure security of web paths in application, login, logout, etc
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .antMatchers("/").hasRole("EMPLOYEE")
                                .antMatchers("/leaders/**").hasRole("MANAGER")
                                .antMatchers("/systems/**").hasRole("ADMIN")
                )
                .and()
                .formLogin()
                .loginPage("/my-login/")
                .loginProcessingUrl("/my-authenticate")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedPage("/access-denied")
                .build();
    }
}
