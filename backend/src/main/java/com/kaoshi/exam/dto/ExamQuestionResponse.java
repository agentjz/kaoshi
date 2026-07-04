package com.kaoshi.exam.dto;

import com.kaoshi.question.dto.QuestionAttachmentResponse;

import java.math.BigDecimal;
import java.util.List;

public record ExamQuestionResponse(
        Long questionId,
        String type,
        String stem,
        String sectionCode,
        String sectionTitle,
        Integer sectionSortOrder,
        String groupCode,
        String groupTitle,
        String groupDirection,
        String groupMaterial,
        Integer groupSortOrder,
        String itemLabel,
        String itemStem,
        BigDecimal score,
        Integer sortOrder,
        List<String> selectedLabels,
        String answerText,
        List<QuestionAttachmentResponse> attachments,
        List<ExamQuestionOptionResponse> options
) {
}

