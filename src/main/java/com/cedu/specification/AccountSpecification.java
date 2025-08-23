package com.cedu.specification;

import com.cedu.dto.account.FilterAccountDto;
import com.cedu.entity.Account;
import org.springframework.data.jpa.domain.Specification;

import static com.cedu.specification.Specs.*;

public class AccountSpecification {

    public static Specification<Account> withFilters(FilterAccountDto f) {

        return where(
                eq("id", f.getId()),
                eq("userId", f.getUserId()),
                eq("parentId", f.getParentId()),
                like("name", f.getName()),
                eq("kind", f.getKind()),
                eq("isActive", f.getIsActive())
        );

    }
}
