package com.example.graphql.util;

import java.time.LocalDate;
import java.time.Period;

public final class AgeUtils {

    private AgeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Calculate age in years from date of birth to current date.
     *
     * @param dateOfBirth the date of birth
     * @return the age in years
     */
    public static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
