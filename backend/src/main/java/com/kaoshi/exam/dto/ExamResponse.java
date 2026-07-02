package com.kaoshi.exam.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public record ExamResponse(
        Long id,
        Long paperId,
        String paperName,
        BigDecimal totalScore,
        String title,
        String description,
        BigDecimal qualifyScore,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer durationMinutes,
        Boolean timeLimit,
        Integer attemptLimit,
        String displayMode,
        String openType,
        List<Long> departmentIds,
        String status
) {
}

