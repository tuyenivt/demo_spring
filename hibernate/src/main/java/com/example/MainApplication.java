package com.example;

import com.example.entity.Student;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class MainApplication {

    private static final String HIBERNATE_CONFIG_XML = "hibernate.cfg.xml";

    public void createStudent(String firstName, String lastName, String email) {
        var sessionFactory = new Configuration().configure(HIBERNATE_CONFIG_XML)
                .addAnnotatedClass(Student.class).buildSessionFactory();
        try (sessionFactory) {
            var session = sessionFactory.getCurrentSession();
            var student = Student.builder().firstName(firstName).lastName(lastName).email(email).build();
            session.beginTransaction();
            session.persist(student);
            session.getTransaction().commit();
        }
    }

    public Student readStudent(int studentId) {
        var sessionFactory = new Configuration().configure(HIBERNATE_CONFIG_XML)
                .addAnnotatedClass(Student.class).buildSessionFactory();
        var session = sessionFactory.getCurrentSession();
        Student student;
        try (sessionFactory) {
            session.beginTransaction();
            student = session.get(Student.class, studentId);
            session.getTransaction().commit();
        }
        return student;
    }

    public List<Student> queryStudent(String firstName) {
        var sessionFactory = new Configuration().configure(HIBERNATE_CONFIG_XML)
                .addAnnotatedClass(Student.class).buildSessionFactory();
        var session = sessionFactory.getCurrentSession();
        List<Student> students;
        try (sessionFactory) {
            session.beginTransaction();
            students = session.createQuery("from Student where firstName like :firstName", Student.class)
                    .setParameter("firstName", "%" + firstName + "%").getResultList();
            session.getTransaction().commit();
        }
        return students;
    }
}
