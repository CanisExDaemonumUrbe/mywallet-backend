package com.cedu.dto.transaction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterTransactionDto {
    private UUID id;
    private UUID userId;
    private Instant from;
    private Instant to;
    private String type;
    private UUID sourceId;
    private Set<UUID> tagsId;
}
