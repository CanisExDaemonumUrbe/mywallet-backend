package com.cedu.dto.journal_entry;

import com.cedu.enums.JournalEntryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestJournalEntryDto {

    @NotNull(message = "user_id is required")
    private UUID userId;

    @NotNull(message = "occurred_at is required")
    private OffsetDateTime occurredAt;

    private OffsetDateTime bookedAt;

    private String description;

    private UUID reversalOfId;

    @NotNull(message = "status is required")
    private JournalEntryStatus status;
}
