package com.cedu.dto.tag;

import lombok.*;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseTagDto {
    private UUID id;
    private String name;
}
