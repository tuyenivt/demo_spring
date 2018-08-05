package com.example.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception { // config user
        @SuppressWarnings("deprecation") // for demo only
        UserBuilder users = User.withDefaultPasswordEncoder();
        auth.inMemoryAuthentication()
            .withUser(users.username("john").password("123456").roles("EMPLOYEE"))
            .withUser(users.username("peter").password("123456").roles("MANAGER"))
            .withUser(users.username("frank").password("123456").roles("ADMIN"));
    }
}