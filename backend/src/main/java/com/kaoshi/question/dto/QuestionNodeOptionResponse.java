package com.kaoshi.question.dto;

public record QuestionNodeOptionResponse(
        Long id,
        String label,
        String content,
        Integer sortOrder
) {
}
