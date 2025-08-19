package com.cedu.specification;

import com.cedu.dto.account.FilterAccountDto;
import com.cedu.entity.Account;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;

public class AccountSpecification {

    public static Specification<Account> withFilters(FilterAccountDto filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
