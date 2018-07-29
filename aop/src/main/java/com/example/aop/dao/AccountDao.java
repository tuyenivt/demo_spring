package com.example.aop.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.aop.entity.Account;

@Component
public class AccountDao {

    public List<Account> find(List<Integer> ids) {
        return new ArrayList<>();
    }

    public List<Account> findOrExceptionIfNotFound(List<Integer> ids) {
        throw new RuntimeException("not found any account " + ids);
    }

    public void add() {

    }

    public void delete(int id) {
        throw new RuntimeException("not allowed delete any account");
    }
}
