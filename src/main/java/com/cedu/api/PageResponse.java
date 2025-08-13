package com.cedu.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> data, Pagination pagination, ApiResponse.Meta meta) {
    
    public static <T> PageResponse<T> of(Page<T> page) {
        var p = new Pagination(page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.hasNext());
        return new PageResponse<>(page.getContent(), p, ApiResponse.Meta.defaultMeta());
    }

    public record Pagination(int page, int size, long totalElements, int totalPages, boolean hasNext) {}
}
