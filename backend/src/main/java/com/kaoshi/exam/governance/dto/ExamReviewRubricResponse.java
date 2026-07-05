package com.kaoshi.exam.governance.dto;

import java.math.BigDecimal;

public record ExamReviewRubricResponse(
        Long id,
        Long examId,
        String title,
        String description,
        BigDecimal maxScore,
        Integer sortOrder
) {
}
