package com.cedu.dto.transaction;

import com.cedu.dto.money_source.ResponseShortMoneySourceDto;
import com.cedu.dto.tag.ResponseTagDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseTransactionDto {
    private UUID id;
    private UUID userId;
    private Instant date;
    private BigDecimal amount;
    private String type;
    private ResponseShortMoneySourceDto source;
    private String description;
    private Set<ResponseTagDto> tags;
}
