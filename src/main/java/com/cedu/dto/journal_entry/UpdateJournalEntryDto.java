package com.cedu.dto.journal_entry;

import com.cedu.enums.JournalEntryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJournalEntryDto {
    private JournalEntryStatus status;
}
