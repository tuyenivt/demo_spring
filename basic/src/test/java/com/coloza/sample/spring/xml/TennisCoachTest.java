package com.coloza.sample.spring.xml;

import com.coloza.sample.spring.Coach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TennisCoachTest {

    private ClassPathXmlApplicationContext context;
    private Coach coach;

    @BeforeAll
    public void setup() {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
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
