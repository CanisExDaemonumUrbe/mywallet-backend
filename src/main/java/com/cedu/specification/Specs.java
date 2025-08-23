package com.cedu.specification;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class Specs {
    private Specs() {}

    public static <T, V> Specification<T> eq(String attr, V value) {
        return value == null ? null :
                (root, q, cb) -> cb.equal(root.get(attr), value);
    }

    public static <T> Specification<T> like(String attr, String value) {
        if (value == null || value.isBlank()) return null;
        String raw = value.toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        String pattern = "%" + raw + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get(attr)), pattern, '\\');
    }


    public static <T, V extends Comparable<? super V>> Specification<T> gte(String attr, V from) {
        return from == null ? null : (root, q, cb) -> cb.greaterThanOrEqualTo(root.get(attr), from);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> lte(String attr, V to) {
        return to == null ? null : (root, q, cb) -> cb.lessThanOrEqualTo(root.get(attr), to);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> between(String attr, V from, V to) {
        if (from == null && to == null) return null;

        return (root, q, cb) -> {
            Path<V> path = root.get(attr);
            if (from != null && to != null) {
                return cb.between(path, from, to); // включительно
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(path, from);
            }
            return cb.lessThanOrEqualTo(path, to);
        };
    }

    @SafeVarargs
    public static <T> Specification<T> where(Specification<T>... parts) {
        Specification<T> result = Specification.where(null);
        for (Specification<T> s : parts) {
            if (s != null) result = result.and(s);
        }
        return result;
    }

}
