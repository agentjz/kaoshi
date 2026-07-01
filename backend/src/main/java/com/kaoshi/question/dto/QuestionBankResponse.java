package com.kaoshi.question.dto;

public record QuestionBankResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        String status
) {
}

