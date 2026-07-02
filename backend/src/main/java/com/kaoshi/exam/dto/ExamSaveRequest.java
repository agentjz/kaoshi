package com.kaoshi.exam.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public record ExamSaveRequest(
        @NotNull Long paperId,
        @NotBlank @Size(max = 128) String title,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.00") BigDecimal qualifyScore,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull @Min(1) Integer durationMinutes,
        @NotNull Boolean timeLimit,
        @Min(1) Integer attemptLimit,
        @NotBlank String displayMode,
        @NotBlank String openType,
        List<Long> departmentIds,
        @NotBlank String status
) {
}

