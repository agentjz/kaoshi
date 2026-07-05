package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamReviewRecheckResponse(
        Long id,
        Long taskId,
        Long resultId,
        String requestedBy,
        String status,
        String reason,
        String resolution,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
