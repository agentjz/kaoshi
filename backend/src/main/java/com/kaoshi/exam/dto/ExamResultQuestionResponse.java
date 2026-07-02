package com.kaoshi.exam.dto;

import com.kaoshi.question.dto.QuestionAttachmentResponse;

import java.math.BigDecimal;
import java.util.List;

public record ExamResultQuestionResponse(
        Long questionId,
        String type,
        String stem,
        String analysis,
        BigDecimal score,
        BigDecimal obtainedScore,
        Integer sortOrder,
        List<String> selectedLabels,
        List<String> correctLabels,
        Boolean correct,
        List<QuestionAttachmentResponse> attachments,
        List<ExamQuestionOptionResponse> options
) {
}
