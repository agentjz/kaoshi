package com.kaoshi.common.page;

import java.util.List;

public record PageResponse<T>(
        List<T> records,
        long total,
        int page,
        int size
) {
}

