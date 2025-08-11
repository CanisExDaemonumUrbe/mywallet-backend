package com.cedu.specification;

import com.cedu.dto.transaction.FilterTransactionDto;
import com.cedu.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(FilterTransactionDto filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }

            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }

            if (filter.getFrom() != null && filter.getTo() != null) {
                predicates.add(cb.between(root.get("date"), filter.getFrom(), filter.getTo()));
            } else if (filter.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), filter.getFrom()));
            } else if (filter.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), filter.getTo()));
            }

            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            if (filter.getSourceId() != null) {
                predicates.add(cb.equal(root.get("source").get("id"), filter.getSourceId()));
            }

            if (filter.getTagsId() != null && !filter.getTagsId().isEmpty()) {
                var tagJoin = root.join("tags");
                predicates.add(tagJoin.get("id").in(filter.getTagsId()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
