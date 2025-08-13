package com.cedu.api;

import java.time.OffsetDateTime;

public record ApiResponse<T>(T data, Meta meta) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, Meta.defaultMeta());
    }

    public record Meta(OffsetDateTime timestamp) {
        public static Meta defaultMeta() {
            return new Meta(OffsetDateTime.now());
        }
    }
}