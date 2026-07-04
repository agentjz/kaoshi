package com.kaoshi.question.dto;

import java.util.List;

public record QuestionContentNodeResponse(
        Long id,
        Long parentId,
        String nodeCode,
        String nodeType,
        String title,
        String direction,
        String material,
        Integer sortOrder,
        List<QuestionNodeOptionResponse> sharedOptions,
        List<QuestionAttachmentResponse> attachments,
        List<QuestionResponse> questions,
        List<QuestionContentNodeResponse> children
) {
}
