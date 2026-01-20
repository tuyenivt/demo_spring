package com.example.modulith.customer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Instant registeredAt;

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
        this.registeredAt = Instant.now();
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }
}
