package com.kaoshi.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExamResultDetailResponse(
        Long id,
        Long attemptId,
        Long examId,
        String examTitle,
        Long userId,
        BigDecimal totalScore,
        BigDecimal obtainedScore,
        Integer correctCount,
        Integer questionCount,
        LocalDateTime submittedAt,
        List<ExamResultQuestionResponse> questions
) {
}
