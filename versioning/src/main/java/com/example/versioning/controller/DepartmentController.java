package com.example.versioning.controller;

import com.example.versioning.exception.ApiVersionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DepartmentController {

    @GetMapping("/location")
    public String getLocation(@RequestHeader(name = "Accept-version", required = false) String versionHeader) {
        if (versionHeader == null || versionHeader.isBlank() || "v2".equals(versionHeader) || "2".equals(versionHeader)) {
            return "Department location v2 is HCM";
        }
        throw new ApiVersionException("Unsupported API version", versionHeader, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/v2/location")
    public String getLocationPathVersioned() {
        return "Department location v2 is HCM";
    }
}
