package com.example.rest.secure.jwt.oauth2.service;

import java.util.List;

import com.example.rest.secure.jwt.oauth2.domain.RandomCity;
import com.example.rest.secure.jwt.oauth2.domain.User;

public interface GenericService {
    User findByUsername(String username);

    List<User> findAllUsers();

    List<RandomCity> findAllRandomCities();
}
