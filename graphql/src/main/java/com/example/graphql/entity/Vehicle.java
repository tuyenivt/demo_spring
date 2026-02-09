package com.example.graphql.entity;

import com.example.graphql.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicle")
public class Vehicle extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VehicleType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;
}
