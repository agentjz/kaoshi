package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamAttemptEventResponse(
        Long id,
        Long examId,
        Long attemptId,
        Long userId,
        String username,
        String actorUsername,
        String action,
        String reason,
        LocalDateTime createdAt
) {
}
