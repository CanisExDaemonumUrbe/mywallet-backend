package com.cedu.specification;

import com.cedu.dto.tag.FilterTagDto;
import com.cedu.entity.Tag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TagSpecification {

    public static Specification<Tag> withFilters(FilterTagDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filterDto.getId()));
            }

            if (filterDto.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filterDto.getUserId()));
            }

            if (filterDto.getName() != null) {
                predicates.add(criteriaBuilder.like(root.get("name"), filterDto.getName()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
