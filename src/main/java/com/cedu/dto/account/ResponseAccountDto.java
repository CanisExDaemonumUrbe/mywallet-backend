package com.cedu.dto.account;

import com.cedu.enums.AccountKind;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseAccountDto {
    private UUID id;
    private UUID userId;
    private UUID parentId;
    private String name;
    private AccountKind kind;
    private Boolean isActive;
}
