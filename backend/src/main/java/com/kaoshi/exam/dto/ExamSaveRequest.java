package com.kaoshi.exam.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ExamSaveRequest(
        @NotNull Long paperId,
        @NotBlank @Size(max = 128) String title,
        @Size(max = 500) String description,
        @NotNull LocalDateTime startTime,
        @NotNull @Future LocalDateTime endTime,
        @NotNull @Min(1) Integer durationMinutes,
        @NotBlank String status
) {
}

