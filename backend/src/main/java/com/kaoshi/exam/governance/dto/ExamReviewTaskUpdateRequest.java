package com.kaoshi.exam.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExamReviewTaskUpdateRequest(
        @NotBlank @Size(max = 32) String status
) {
}
