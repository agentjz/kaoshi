package com.kaoshi.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequest(
        @Min(1) int page,
        @Min(1) @Max(100) int size,
        String keyword
) {
    public int offset() {
        return (page - 1) * size;
    }

    public String keywordLike() {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return "%" + keyword.trim() + "%";
    }
}

