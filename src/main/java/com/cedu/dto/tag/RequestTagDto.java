package com.cedu.dto.tag;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTagDto {
    private UUID userId;
    private String name;
}
