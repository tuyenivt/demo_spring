package com.example.rest.secure.jwt.oauth2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.rest.secure.jwt.oauth2.domain.RandomCity;
import com.example.rest.secure.jwt.oauth2.domain.User;
import com.example.rest.secure.jwt.oauth2.repository.RandomCityRepository;
import com.example.rest.secure.jwt.oauth2.repository.UserRepository;

@Service
public class GenericServiceImpl implements GenericService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RandomCityRepository randomCityRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    public List<RandomCity> findAllRandomCities() {
        return (List<RandomCity>) randomCityRepository.findAll();
    }
}
