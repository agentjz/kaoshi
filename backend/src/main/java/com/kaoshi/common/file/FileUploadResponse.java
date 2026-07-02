package com.kaoshi.common.file;

public record FileUploadResponse(
        String fileName,
        String fileUrl,
        String mediaType
) {
}
