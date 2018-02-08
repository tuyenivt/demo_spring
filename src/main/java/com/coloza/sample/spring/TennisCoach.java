package com.coloza.sample.spring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("myTennisCoach")
public class TennisCoach implements Coach {

    private FortuneService fortuneService;

    @Autowired
    public TennisCoach(FortuneService fortuneService) {
        this.fortuneService = fortuneService;
    }

    @Override
    public String getDailyWorkout() {
        return "Practice your backhand volly";
    }

    @Override
    public String getDailyFortune() {
        return fortuneService.getFortune();
    }

    @PostConstruct
    public void deMyStartStuff() {
        System.out.println("TennisCoach: inside method doMyStartStuff");
    }

    @PreDestroy
    public void deMyCleanStuff() {
        System.out.println("TennisCoach: inside method doMyCleanStuff");
    }

}
