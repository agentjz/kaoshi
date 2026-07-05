package com.kaoshi.common.file;

import java.time.LocalDateTime;

public record FileAssetResponse(
        Long id,
        String originalName,
        String fileUrl,
        String mediaType,
        String usageType,
        String uploadedBy,
        LocalDateTime uploadedAt
) {
}
