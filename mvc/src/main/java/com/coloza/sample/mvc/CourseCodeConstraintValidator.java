package com.coloza.sample.mvc;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CourseCodeConstraintValidator implements ConstraintValidator<CourseCode, String> {

    private String[] coursePrefixs;

    @Override
    public void initialize(CourseCode courseCode) {
        coursePrefixs = courseCode.value();
    }

    @Override
    public boolean isValid(String code, ConstraintValidatorContext validatorContext) {
        if (code == null) {
            return true;
        }
        for (String coursePrefix : coursePrefixs) {
            if (code.startsWith(coursePrefix)) {
                return true;
            }
        }
        return false;
    }

}
