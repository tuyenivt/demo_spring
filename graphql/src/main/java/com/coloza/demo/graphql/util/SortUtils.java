package com.coloza.demo.graphql.util;

import com.coloza.demo.graphql.dto.sort.SortDirection;
import com.coloza.demo.graphql.dto.sort.StudentSort;
import com.coloza.demo.graphql.dto.sort.VehicleSort;
import org.springframework.data.domain.Sort;

public final class SortUtils {

    private SortUtils() {
    }

    public static Sort toSort(StudentSort studentSort) {
        if (studentSort == null || studentSort.field() == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = studentSort.getDirectionOrDefault() == SortDirection.ASC
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, studentSort.field().getFieldName());
    }

    public static Sort toSort(VehicleSort vehicleSort) {
        if (vehicleSort == null || vehicleSort.field() == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = vehicleSort.getDirectionOrDefault() == SortDirection.ASC
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, vehicleSort.field().getFieldName());
    }
}
