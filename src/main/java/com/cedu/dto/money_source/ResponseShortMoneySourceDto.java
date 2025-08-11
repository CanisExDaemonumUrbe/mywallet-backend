package com.cedu.dto.money_source;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseShortMoneySourceDto {
    private UUID id;
    private String name;
}
