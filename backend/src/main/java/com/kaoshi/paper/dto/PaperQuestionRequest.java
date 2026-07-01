package com.kaoshi.paper.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaperQuestionRequest(
        @NotNull Long questionId,
        @NotNull @DecimalMin("0.01") BigDecimal score
) {
}

