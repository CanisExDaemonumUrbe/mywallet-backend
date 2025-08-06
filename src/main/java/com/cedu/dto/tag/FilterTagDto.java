package com.cedu.dto.tag;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterTagDto {
    private UUID id;
    private UUID userId;
    private String name;
}
