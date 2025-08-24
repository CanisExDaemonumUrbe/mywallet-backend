package com.cedu.dto.posting;

import com.cedu.enums.PostingSide;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterPostingDto {
    private UUID id;
    private UUID userId;
    private UUID journalEntryId;
    private UUID accountId;
    private PostingSide side;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
}
