package com.coloza.sample.mvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @Value("${spring.application.name}")
    String appName;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", appName);
        return "home/index";
    }

}
