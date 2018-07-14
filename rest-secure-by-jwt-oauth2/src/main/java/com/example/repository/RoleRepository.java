package com.example.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.domain.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
}
