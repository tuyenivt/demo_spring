package com.example.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/leaders")
public class LeadersController {

    @GetMapping("/")
    public String index() {
        return "leaders/index";
    }
}
