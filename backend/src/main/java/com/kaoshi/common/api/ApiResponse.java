package com.kaoshi.common.api;

import java.time.Instant;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data, Instant.now());
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.code(), errorCode.message(), null, Instant.now());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.code(), message, null, Instant.now());
    }
}

