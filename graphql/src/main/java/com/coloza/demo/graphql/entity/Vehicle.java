package com.coloza.demo.graphql.entity;

import com.coloza.demo.graphql.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VehicleType type;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
}
