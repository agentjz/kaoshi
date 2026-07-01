package com.kaoshi.exam.dto;

import java.time.LocalDateTime;

public record ExamResponse(
        Long id,
        Long paperId,
        String paperName,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer durationMinutes,
        String status
) {
}

