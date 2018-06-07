package com.example.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.domain.RandomCity;

public interface RandomCityRepository extends CrudRepository<RandomCity, Long> {
}
