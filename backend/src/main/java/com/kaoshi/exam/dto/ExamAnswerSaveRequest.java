package com.kaoshi.exam.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExamAnswerSaveRequest(
        @NotNull Long questionId,
        @NotNull List<String> selectedLabels
) {
}
