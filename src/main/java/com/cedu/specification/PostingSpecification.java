package com.cedu.specification;

import com.cedu.dto.posting.FilterPostingDto;
import com.cedu.entity.Posting;
import org.springframework.data.jpa.domain.Specification;

import static com.cedu.specification.Specs.*;


public class PostingSpecification {
    public static Specification<Posting> withFilters(FilterPostingDto f) {
        return where(
                eq("id", f.getId()),
                eq("userId", f.getUserId()),
                eq("journalEntryId", f.getJournalEntryId()),
                eq("accountId", f.getAccountId()),
                eq("side", f.getSide()),
                between("amount", f.getAmountFrom(), f.getAmountTo())
        );
    }
}
