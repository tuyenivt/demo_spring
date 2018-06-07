package com.coloza.sample.spring.xml;

import org.junit.Before;
import org.junit.Test;

import com.coloza.sample.spring.Coach;

public class DefaultCoachTest extends BaseXmlTest {

    private Coach coach;

    @Before
    public void setup() {
        super.setup();
        coach = context.getBean("defaultCoach", Coach.class);
    }

    @Test
    public void testGetDailyWorkout() {
        System.out.println(coach.getDailyWorkout());
    }

}
