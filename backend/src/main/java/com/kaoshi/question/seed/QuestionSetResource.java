package com.kaoshi.question.seed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

record QuestionSetResource(
        String code,
        String name,
        List<CategoryResource> categories,
        List<BankResource> banks,
        List<SectionResource> sections,
        List<ExamResource> exams
) {
    record CategoryResource(String code, String name, String description, Integer sortOrder) {
    }

    record BankResource(String code, String categoryCode, String name, String description, String status) {
    }

    record SectionResource(
            String code,
            String title,
            String direction,
            String material,
            Integer sortOrder,
            List<AttachmentResource> attachments,
            List<GroupResource> groups
    ) {
    }

    record GroupResource(
            String groupCode,
            String groupTitle,
            String groupDirection,
            String groupMaterial,
            Integer groupSortOrder,
            String bankCode,
            List<OptionResource> sharedOptions,
            List<AttachmentResource> attachments,
            List<QuestionResource> items
    ) {
    }

    record QuestionResource(
            String code,
            String type,
            String stem,
            String itemLabel,
            String itemStem,
            List<String> answerLabels,
            String analysis,
            String difficulty,
            String status,
            List<OptionResource> options,
            List<AttachmentResource> attachments
    ) {
    }

    record OptionResource(String label, String content, boolean correct, Integer sortOrder) {
    }

    record AttachmentResource(String fileName, String fileUrl, String mediaType, Integer sortOrder) {
    }

    record ExamResource(
            String code,
            String title,
            String description,
            BigDecimal qualifyScore,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer durationMinutes,
            Boolean timeLimit,
            Integer attemptLimit,
            String displayMode,
            String questionOrderMode,
            String openType,
            String status,
            List<PaperQuestionResource> paperQuestions
    ) {
    }

    record PaperQuestionResource(String questionCode, BigDecimal score, Integer sortOrder) {
    }
}
