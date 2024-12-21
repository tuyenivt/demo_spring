package com.coloza.sample.spring;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("myTennisCoach")
public class TennisCoach implements Coach {

    @Autowired
    @Qualifier("happyFortuneService")
    private FortuneService fortuneService;

    public TennisCoach() {
        System.out.println("TennisCoach: inside default contructor");
    }

    /*
    @Autowired
    public void tryToSetFortuneService(FortuneService fortuneService) {
        System.out.println("TennisCoach: inside tryToSetFortuneService method");
        this.fortuneService = fortuneService;
    }
    */

    /*
    @Autowired
    public void setFortuneService(FortuneService fortuneService) {
        System.out.println("TennisCoach: inside setFortuneService medthod");
        this.fortuneService = fortuneService;
    }
    */

    /*
    @Autowired
    public TennisCoach(FortuneService fortuneService) {
        this.fortuneService = fortuneService;
    }
    */

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
