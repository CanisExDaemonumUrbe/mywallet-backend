package com.cedu.dto.journal_entry;

import com.cedu.enums.JournalEntryStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterJournalEntryDto {
    private UUID id;
    private UUID userId;
    private OffsetDateTime occurredFrom;
    private OffsetDateTime occurredTo;
    private OffsetDateTime bookedFrom;
    private OffsetDateTime bookedTo;
    private String description;
    private UUID reversalOfId;
    private JournalEntryStatus status;
}
