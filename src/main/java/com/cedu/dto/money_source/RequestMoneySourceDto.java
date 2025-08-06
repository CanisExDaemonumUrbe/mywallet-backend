package com.cedu.dto.money_source;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestMoneySourceDto {
    private UUID userId;
    private String name;
    private String type;
    private String currency;
    private String description;
}
