package com.example.rest.secure.jwt.oauth2.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.rest.secure.jwt.oauth2.domain.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
}
