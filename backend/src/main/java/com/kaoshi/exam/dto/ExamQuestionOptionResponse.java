package com.kaoshi.exam.dto;

public record ExamQuestionOptionResponse(
        Long id,
        String label,
        String content,
        Integer sortOrder
) {
}

