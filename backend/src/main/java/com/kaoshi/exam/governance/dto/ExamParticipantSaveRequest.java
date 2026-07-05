package com.kaoshi.exam.governance.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExamParticipantSaveRequest(
        @NotNull List<Long> userIds
) {
}
