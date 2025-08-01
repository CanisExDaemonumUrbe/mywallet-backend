package com.cedu.dto.money_source;

import lombok.*;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoneySourceFilterDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String type;
    private String currency;
}

