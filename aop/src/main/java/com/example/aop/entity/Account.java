package com.example.aop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private int id;
    private String name;

    @Override
    public String toString() {
        return "Account [id=" + id + ", name=" + name + "]";
    }
}
