package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamSecurityEventResponse(
        Long id,
        Long examId,
        Long attemptId,
        Long userId,
        String username,
        String eventType,
        String severity,
        String detail,
        LocalDateTime occurredAt
) {
}
