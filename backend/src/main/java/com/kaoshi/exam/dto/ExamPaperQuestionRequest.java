package com.kaoshi.exam.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExamPaperQuestionRequest(
        @NotNull Long questionId,
        @NotNull @DecimalMin("0.01") BigDecimal score,
        @NotNull @Min(1) Integer sortOrder
) {
}
