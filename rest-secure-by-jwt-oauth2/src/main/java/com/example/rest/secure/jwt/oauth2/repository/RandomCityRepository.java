package com.example.rest.secure.jwt.oauth2.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.rest.secure.jwt.oauth2.domain.RandomCity;

public interface RandomCityRepository extends CrudRepository<RandomCity, Long> {
}
