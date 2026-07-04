package com.kaoshi.question;

import com.kaoshi.question.dto.QuestionAttachmentRequest;
import com.kaoshi.question.dto.QuestionNodeOptionRequest;
import com.kaoshi.question.dto.QuestionOptionRequest;

import java.util.List;
import java.util.Map;

record QuestionBankPackageWorkbook(
        List<SectionRow> sections,
        List<GroupRow> groups,
        List<ItemRow> items,
        Map<String, List<QuestionNodeOptionRequest>> nodeOptions,
        Map<String, List<QuestionOptionSeed>> itemOptions,
        Map<String, List<QuestionAttachmentRequest>> nodeAttachments,
        Map<String, List<QuestionAttachmentRequest>> itemAttachments
) {
    List<QuestionNodeOptionRequest> nodeOptions(String nodeCode) {
        return nodeOptions.getOrDefault(nodeCode, List.of());
    }

    List<QuestionAttachmentRequest> nodeAttachments(String nodeCode) {
        return nodeAttachments.getOrDefault(nodeCode, List.of());
    }

    List<QuestionOptionRequest> itemOptions(String itemCode, List<String> answerLabels) {
        return itemOptions.getOrDefault(itemCode, List.of()).stream()
                .map(option -> new QuestionOptionRequest(
                        option.label(),
                        option.content(),
                        option.correct() || answerLabels.contains(option.label())
                ))
                .toList();
    }

    List<QuestionAttachmentRequest> itemAttachments(String itemCode) {
        return itemAttachments.getOrDefault(itemCode, List.of());
    }

    record SectionRow(String code, String title, String direction, String material, int sortOrder) {
    }

    record GroupRow(String code, String sectionCode, String title, String direction, String material, int sortOrder) {
    }

    record ItemRow(String code, String groupCode, String label, String type, String stem, String itemStem,
                   List<String> answerLabels, String analysis, String difficulty, String status) {
    }

    record QuestionOptionSeed(String label, String content, boolean correct) {
    }
}
