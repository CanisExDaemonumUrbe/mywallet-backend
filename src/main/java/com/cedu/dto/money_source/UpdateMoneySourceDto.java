package com.cedu.dto.money_source;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMoneySourceDto {
    private String name;
    private String type;
    private String currency;
    private String description;
}
