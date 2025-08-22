package com.cedu.specification;

import org.springframework.data.jpa.domain.Specification;

public final class Specs {
    private Specs() {}

    public static <T, V> Specification<T> eq(String attr, V value) {
        return value == null ? null :
                (root, q, cb) -> cb.equal(root.get(attr), value);
    }

    public static <T> Specification<T> like(String attr, String value) {
        return (value == null || value.isBlank()) ? null :
                (root, q, cb) -> cb.like(root.get(attr), value);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> gte(String attr, V from) {
        return from == null ? null : (root, q, cb) -> cb.greaterThanOrEqualTo(root.get(attr), from);
    }

    public static <T, V extends Comparable<? super V>> Specification<T> lte(String attr, V to) {
        return to == null ? null : (root, q, cb) -> cb.lessThanOrEqualTo(root.get(attr), to);
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
