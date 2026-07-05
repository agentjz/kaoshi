package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamParticipantResponse(
        Long userId,
        String username,
        String displayName,
        String departmentName,
        String status,
        Integer extraMinutes,
        Integer extraAttempts,
        String reason,
        LocalDateTime assignedAt
) {
}
