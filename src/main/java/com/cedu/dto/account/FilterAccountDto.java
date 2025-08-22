package com.cedu.dto.account;

import com.cedu.enums.AccountKind;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FilterAccountDto {
    private UUID id;
    private UUID userId;
    private UUID parentId;
    private String name;
    private AccountKind kind;
    private Boolean isActive;
}
