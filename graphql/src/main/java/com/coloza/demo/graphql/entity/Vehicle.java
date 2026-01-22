package com.coloza.demo.graphql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {
    @Column(name = "type", nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}
