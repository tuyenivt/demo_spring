package com.example.multiple.databases.oldDemo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "old_product")
public class OldProduct implements Serializable {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "price")
    private Double price;

    @Column(name = "quality")
    private Long quality;

    @Column(name = "date_of_manufacture")
    private LocalDateTime dateOfManufacture;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
