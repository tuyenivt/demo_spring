package com.example.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/my-login")
    public String index() {
        return "login/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "login/access-denied";
    }

}
