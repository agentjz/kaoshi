package com.kaoshi.exam.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ExamReviewRubricRequest(
        @NotBlank @Size(max = 128) String title,
        @Size(max = 1000) String description,
        @NotNull @PositiveOrZero BigDecimal maxScore,
        Integer sortOrder
) {
}
