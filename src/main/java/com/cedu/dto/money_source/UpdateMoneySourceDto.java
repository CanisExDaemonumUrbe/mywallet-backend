package com.cedu.dto.money_source;


import lombok.*;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateMoneySourceDto {
    private String name;
    private String type;
    private String currency;
    private String description;
}
