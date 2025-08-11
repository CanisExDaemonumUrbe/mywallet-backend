package com.cedu.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTransactionDto {
    private UUID userId;
    private Instant date;
    private BigDecimal amount;
    private String type;
    private UUID moneySourceId;
    private String description;
    private Set<UUID> tagsIds;
}
