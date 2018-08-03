package com.example.rest.secure.jwt.oauth2.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.rest.secure.jwt.oauth2.domain.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
}
