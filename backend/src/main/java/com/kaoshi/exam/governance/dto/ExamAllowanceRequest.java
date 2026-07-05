package com.kaoshi.exam.governance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ExamAllowanceRequest(
        @Min(0) @Max(1440) Integer extraMinutes,
        @Min(0) @Max(100) Integer extraAttempts,
        String reason
) {
}
