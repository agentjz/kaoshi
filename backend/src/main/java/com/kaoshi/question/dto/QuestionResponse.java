package com.kaoshi.question.dto;

import java.math.BigDecimal;
import java.util.List;

public record QuestionResponse(
        Long id,
        Long bankId,
        String bankName,
        String type,
        String stem,
        String analysis,
        BigDecimal score,
        String difficulty,
        String status,
        List<QuestionOptionResponse> options,
        List<QuestionAttachmentResponse> attachments
) {
}

