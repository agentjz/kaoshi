package com.kaoshi.exam.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AnswerSubmitItem(
        @NotNull Long questionId,
        @NotEmpty List<String> selectedLabels
) {
}

