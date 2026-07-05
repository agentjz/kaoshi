package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamResultPolicyResponse(
        Long examId,
        Boolean visibleToStudents,
        Boolean showAnswers,
        Boolean showAnalysis,
        LocalDateTime releaseTime,
        LocalDateTime updatedAt
) {
}
