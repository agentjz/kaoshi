package com.kaoshi.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionBankSaveRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 500) String description,
        @NotBlank String status
) {
}

