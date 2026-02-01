package com.example.versioning.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DepartmentController {

    @RequestMapping(value = "/location", headers = "Accept-version=v2")
    public String getLocation() {
        return "Department location v2 is HCM";
    }
}
