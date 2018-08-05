package com.example.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override // configure users
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        @SuppressWarnings("deprecation") // for demo only
        UserBuilder users = User.withDefaultPasswordEncoder();
        auth.inMemoryAuthentication()
            .withUser(users.username("john").password("123456").roles("EMPLOYEE"))
            .withUser(users.username("peter").password("123456").roles("MANAGER"))
            .withUser(users.username("frank").password("123456").roles("ADMIN"));
    }

    @Override // configure security of web paths in application, login, logout, etc
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/my-login/")
                .loginProcessingUrl("/my-authenticate")
                .permitAll()
            .and()
                .logout()
                .permitAll()
                ;
    }
}
