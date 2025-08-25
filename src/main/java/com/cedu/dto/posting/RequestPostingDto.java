package com.cedu.dto.posting;

import com.cedu.enums.PostingSide;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPostingDto {

    @NotNull(message = "user_id is required")
    private UUID userId;

    @NotNull(message = "journal_entry_id is required")
    private UUID journalEntryId;

    @NotNull(message = "account_id is required")
    private UUID accountId;

    @NotNull(message = "side is required")
    private PostingSide side;

    @NotNull(message = "amount is required")
    private BigDecimal amount;
}
