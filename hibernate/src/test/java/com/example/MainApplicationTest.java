package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainApplicationTest {

    private final MainApplication app = new MainApplication();

    @BeforeEach
    void setup() {
        System.setProperty("hibernate.generate_statistics", "true");
    }

    @Test
    void testCreateStudent() {
        app.createStudent("firstName", "lastName", "email@gmail.com");
    }

    @Test
    void testReadStudent() {
        var student = app.readStudent(1);
        System.out.println("Got: " + (student == null ? "null" : student.getFirstName() + " " + student.getLastName()));
    }

    @Test
    void testQueryStudent() {
        var students = app.queryStudent("name");
        System.out.println("Got: " + (students == null || students.isEmpty() ? "0" : students.size()) + " student(s)");
    }
}
