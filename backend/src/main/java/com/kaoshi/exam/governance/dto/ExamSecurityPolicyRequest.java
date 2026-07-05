package com.kaoshi.exam.governance.dto;

public record ExamSecurityPolicyRequest(
        Boolean requireFullscreen,
        Boolean forbidCopyPaste,
        Boolean trackFocusLoss,
        Integer maxFocusLossCount,
        Boolean deviceCheckRequired
) {
}
