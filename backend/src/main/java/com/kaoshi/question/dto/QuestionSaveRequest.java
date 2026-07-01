package com.kaoshi.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record QuestionSaveRequest(
        @NotNull Long bankId,
        @NotBlank String type,
        @NotBlank String stem,
        String analysis,
        @NotNull @DecimalMin("0.01") BigDecimal score,
        @NotBlank String difficulty,
        @NotBlank String status,
        @NotEmpty List<@Valid QuestionOptionRequest> options,
        List<@Valid QuestionAttachmentRequest> attachments
) {
}

