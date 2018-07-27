package com.example.aop.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.aop.entity.Account;

@Component
public class AccountDao {

    public void add() {

    }

    public List<Account> find(List<Integer> ids) {
        return new ArrayList<>();
    }
}
