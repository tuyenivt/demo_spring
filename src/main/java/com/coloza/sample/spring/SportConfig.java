package com.coloza.sample.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.coloza.sample.spring")
@PropertySource("application.properties")
public class SportConfig {

}
