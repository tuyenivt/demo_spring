package com.coloza.sample.spring.config;

import org.junit.Before;
import org.junit.Test;

import com.coloza.sample.spring.Coach;

public class TennisCoachTest extends BaseConfigTest {

    private Coach coach;

    @Before
    public void setup() {
        super.setup();
        coach = context.getBean("myTennisCoach", Coach.class);
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
