package com.kaoshi.question.dto;

public record QuestionBankPackageImportResponse(
        Long bankId,
        String bankName,
        int nodeCount,
        int questionCount
) {
}
