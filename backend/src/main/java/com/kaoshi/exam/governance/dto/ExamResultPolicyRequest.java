package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamResultPolicyRequest(
        Boolean visibleToStudents,
        Boolean showAnswers,
        Boolean showAnalysis,
        LocalDateTime releaseTime
) {
}
