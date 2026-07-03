package com.kaoshi.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionCategorySaveRequest(
        @NotBlank @Size(max = 64) String name,
        @Size(max = 255) String description,
        Integer sortOrder
) {
}
