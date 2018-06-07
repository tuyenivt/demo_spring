package com.coloza.sample.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HappyFortuneService implements FortuneService {

    @Value("${account.email}")
    private String email;

    @Override
    public String getFortune() {
        return "Today is your lucky day! (" + email + ")";
    }

}
