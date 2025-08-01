package com.cedu.specification;

import com.cedu.dto.money_source.MoneySourceFilterDto;
import com.cedu.entity.MoneySource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MoneySourceSpecification {

    public static Specification<MoneySource> withFilters(MoneySourceFilterDto filterDto) {
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

            if (filterDto.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filterDto.getType()));
            }

            if (filterDto.getCurrency() != null) {
                predicates.add(criteriaBuilder.equal(root.get("currency"), filterDto.getCurrency()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
