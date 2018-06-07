package com.coloza.sample.spring;

import org.springframework.stereotype.Component;

@Component
public class DefaultCoach implements Coach {

    @Override
    public String getDailyWorkout() {
        return "Practice...";
    }

    @Override
    public String getDailyFortune() {
        return null;
    }

}
