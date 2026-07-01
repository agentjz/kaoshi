package com.kaoshi.question.dto;

public record QuestionOptionResponse(
        Long id,
        String label,
        String content,
        Boolean correct,
        Integer sortOrder
) {
}

