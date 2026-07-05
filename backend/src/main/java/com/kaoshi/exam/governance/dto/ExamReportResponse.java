package com.kaoshi.exam.governance.dto;

import java.math.BigDecimal;

public record ExamReportResponse(
        Long examId,
        Integer participantCount,
        Integer submittedCount,
        Integer pendingReviewCount,
        BigDecimal averageScore,
        BigDecimal maxScore,
        BigDecimal minScore,
        BigDecimal passRate
) {
}
