package com.kaoshi.question.dto;

import java.util.List;

public record QuestionContentTreeResponse(
        Long bankId,
        String bankName,
        List<QuestionContentNodeResponse> sections,
        List<QuestionResponse> ungroupedQuestions
) {
}
