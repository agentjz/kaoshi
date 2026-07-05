package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamReviewTaskResponse(
        Long id,
        Long resultId,
        Long examId,
        String examTitle,
        Long reviewerId,
        String reviewerUsername,
        String status,
        String studentName,
        LocalDateTime assignedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {
}
