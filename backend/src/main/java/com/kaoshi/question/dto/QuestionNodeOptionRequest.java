package com.kaoshi.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionNodeOptionRequest(
        @NotBlank @Size(max = 8) String label,
        @NotBlank String content
) {
}
