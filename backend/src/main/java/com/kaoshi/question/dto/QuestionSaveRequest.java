package com.kaoshi.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuestionSaveRequest(
        @NotNull Long bankId,
        @NotBlank String type,
        @NotBlank String stem,
        String sectionCode,
        String sectionTitle,
        Integer sectionSortOrder,
        String groupCode,
        String groupTitle,
        String groupDirection,
        String groupMaterial,
        Integer groupSortOrder,
        String itemLabel,
        String itemStem,
        String analysis,
        @NotBlank String difficulty,
        @NotBlank String status,
        List<@Valid QuestionOptionRequest> options,
        List<@Valid QuestionAttachmentRequest> attachments
) {
}

