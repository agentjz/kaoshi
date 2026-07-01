package com.kaoshi.paper.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaperResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal totalScore,
        Integer durationMinutes,
        String status,
        List<PaperQuestionResponse> questions
) {
}

