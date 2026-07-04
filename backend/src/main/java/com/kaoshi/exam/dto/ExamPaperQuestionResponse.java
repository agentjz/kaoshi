package com.kaoshi.exam.dto;

import java.math.BigDecimal;

public record ExamPaperQuestionResponse(
        Long questionId,
        Long bankId,
        String bankName,
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
        Integer sortOrder
) {
}
