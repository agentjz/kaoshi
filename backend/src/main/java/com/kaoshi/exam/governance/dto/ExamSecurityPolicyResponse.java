package com.kaoshi.exam.governance.dto;

import java.time.LocalDateTime;

public record ExamSecurityPolicyResponse(
        Long examId,
        Boolean requireFullscreen,
        Boolean forbidCopyPaste,
        Boolean trackFocusLoss,
        Integer maxFocusLossCount,
        Boolean deviceCheckRequired,
        LocalDateTime updatedAt
) {
}
