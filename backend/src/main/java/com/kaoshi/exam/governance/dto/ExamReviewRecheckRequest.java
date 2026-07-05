package com.kaoshi.exam.governance.dto;

import jakarta.validation.constraints.Size;

public record ExamReviewRecheckRequest(
        @Size(max = 1000) String reason,
        @Size(max = 1000) String resolution,
        @Size(max = 32) String status
) {
}
