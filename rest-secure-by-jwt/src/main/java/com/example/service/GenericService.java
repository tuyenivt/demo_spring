package com.example.service;

import java.util.List;

import com.example.domain.RandomCity;
import com.example.domain.User;

public interface GenericService {
    User findByUsername(String username);

    List<User> findAllUsers();

    List<RandomCity> findAllRandomCities();
}
