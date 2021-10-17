package com.coloza.sample.spring.config;

import com.coloza.sample.spring.Coach;
import com.coloza.sample.spring.SportConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TennisCoachTest {

    private AnnotationConfigApplicationContext context;
    private Coach coach;

    @BeforeEach
    public void setup() {
        context = new AnnotationConfigApplicationContext(SportConfig.class);
        coach = context.getBean("myTennisCoach", Coach.class);
    }

    @AfterAll
    public void teardown() {
        context.close();
    }

    @Test
    public void testGetDailyWorkout() {
        System.out.println(coach.getDailyWorkout());
    }

    @Test
    public void testGetDailyFortune() {
        System.out.println(coach.getDailyFortune());
    }

}
