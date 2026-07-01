package com.kaoshi.paper.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PaperSaveRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 500) String description,
        @NotNull @Min(1) Integer durationMinutes,
        @NotBlank String status,
        @NotEmpty List<@Valid PaperQuestionRequest> questions
) {
}

