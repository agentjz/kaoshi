package com.kaoshi.paper.dto;

import java.math.BigDecimal;

public record PaperQuestionResponse(
        Long id,
        Long questionId,
        String questionType,
        String stem,
        BigDecimal score,
        Integer sortOrder
) {
}

