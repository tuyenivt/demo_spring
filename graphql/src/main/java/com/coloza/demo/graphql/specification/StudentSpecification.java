package com.coloza.demo.graphql.specification;

import com.coloza.demo.graphql.dto.filter.DateTimeFilter;
import com.coloza.demo.graphql.dto.filter.StringFilter;
import com.coloza.demo.graphql.dto.filter.StudentFilter;
import com.coloza.demo.graphql.entity.Student;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class StudentSpecification {

    private StudentSpecification() {
    }

    public static Specification<Student> fromFilter(StudentFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                addStringPredicates(predicates, cb, root.get("name"), filter.name());
            }

            if (filter.address() != null) {
                addStringPredicates(predicates, cb, root.get("address"), filter.address());
            }

            if (filter.createdAt() != null) {
                addDateTimePredicates(predicates, cb, root.get("createdAt"), filter.createdAt());
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addStringPredicates(List<Predicate> predicates, CriteriaBuilder cb,
                                            Path<String> path, StringFilter filter) {
        if (filter.eq() != null) {
            predicates.add(cb.equal(path, filter.eq()));
        }
        if (filter.contains() != null) {
            predicates.add(cb.like(cb.lower(path), "%" + filter.contains().toLowerCase() + "%"));
        }
        if (filter.startsWith() != null) {
            predicates.add(cb.like(cb.lower(path), filter.startsWith().toLowerCase() + "%"));
        }
        if (filter.endsWith() != null) {
            predicates.add(cb.like(cb.lower(path), "%" + filter.endsWith().toLowerCase()));
        }
        if (filter.in() != null && !filter.in().isEmpty()) {
            predicates.add(path.in(filter.in()));
        }
    }

    private static void addDateTimePredicates(List<Predicate> predicates, CriteriaBuilder cb,
                                              Path<OffsetDateTime> path, DateTimeFilter filter) {
        if (filter.eq() != null) {
            predicates.add(cb.equal(path, filter.eq()));
        }
        if (filter.gt() != null) {
            predicates.add(cb.greaterThan(path, filter.gt()));
        }
        if (filter.gte() != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, filter.gte()));
        }
        if (filter.lt() != null) {
            predicates.add(cb.lessThan(path, filter.lt()));
        }
        if (filter.lte() != null) {
            predicates.add(cb.lessThanOrEqualTo(path, filter.lte()));
        }
        if (filter.between() != null) {
            predicates.add(cb.between(path, filter.between().start(), filter.between().end()));
        }
    }
}
