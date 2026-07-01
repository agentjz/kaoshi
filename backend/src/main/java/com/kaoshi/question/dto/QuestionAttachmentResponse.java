package com.kaoshi.question.dto;

public record QuestionAttachmentResponse(
        Long id,
        String fileName,
        String fileUrl,
        String mediaType,
        Integer sortOrder
) {
}

