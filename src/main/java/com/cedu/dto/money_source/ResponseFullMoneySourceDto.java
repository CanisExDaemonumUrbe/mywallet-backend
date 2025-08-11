package com.cedu.dto.money_source;

import java.util.UUID;

import lombok.*;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseFullMoneySourceDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String type;
    private String currency;
    private String description;
}
