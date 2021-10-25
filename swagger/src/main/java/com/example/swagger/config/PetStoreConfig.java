package com.example.swagger.config;

import feign.Client;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.swagger.petstore.api.PetApi;
import io.swagger.petstore.api.StoreApi;
import io.swagger.petstore.api.UserApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.pet-store")
public class PetStoreConfig {

    private String baseUrl;
    private String username;
    private String password;

    private final Client client;

    @Bean
    public PetApi getPetApi() {
        return Feign.builder()
                .client(client)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .target(PetApi.class, baseUrl);
    }

    @Bean
    public StoreApi getStoreApi() {
        return Feign.builder()
                .client(client)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .target(StoreApi.class, baseUrl);
    }

    @Bean
    public UserApi getUserApi() {
        return Feign.builder()
                .client(client)
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .target(UserApi.class, baseUrl);
    }
}
