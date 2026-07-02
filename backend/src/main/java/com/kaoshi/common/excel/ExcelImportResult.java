package com.kaoshi.common.excel;

import java.util.List;

public record ExcelImportResult(
        int successCount,
        int failureCount,
        List<String> errors
) {
}
