package com.cedu.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> data, Pagination pagination, ApiResponse.Meta meta) {
    
    public static <T> PageResponse<T> of(Page<T> page) {
        var p = new Pagination(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
        return new PageResponse<>(page.getContent(), p, ApiResponse.Meta.defaultMeta());
    }

    public record Pagination(
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("total_elements") long totalElements,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("has_next") boolean hasNext
    ) {}
}
