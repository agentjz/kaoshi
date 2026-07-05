package com.kaoshi.exam.governance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExamSecurityEventRequest(
        Long attemptId,
        @NotBlank @Size(max = 64) String eventType,
        @Size(max = 20) String severity,
        @Size(max = 1000) String detail
) {
}
