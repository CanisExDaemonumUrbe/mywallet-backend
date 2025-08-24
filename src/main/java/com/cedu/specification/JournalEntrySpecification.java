package com.cedu.specification;

import com.cedu.dto.journal_entry.FilterJournalEntryDto;
import com.cedu.entity.JournalEntry;
import org.springframework.data.jpa.domain.Specification;

import static com.cedu.specification.Specs.*;

public class JournalEntrySpecification {

    public static Specification<JournalEntry> withFilters(FilterJournalEntryDto f) {
        return where(
                eq("id", f.getId()),
                eq("userId", f.getUserId()),
                between("occurredAt", f.getOccurredFrom(), f.getOccurredTo()),
                between("bookedAt", f.getBookedFrom(), f.getBookedTo()),
                like("description", f.getDescription()),
                eq("reversalOfId", f.getReversalOfId()),
                eq("status", f.getStatus())
        );
    }
}
