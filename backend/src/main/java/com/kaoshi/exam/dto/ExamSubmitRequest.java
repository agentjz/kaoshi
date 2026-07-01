package com.kaoshi.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ExamSubmitRequest(
        @NotEmpty List<@Valid AnswerSubmitItem> answers
) {
}

