package com.kaoshi.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionNodeSaveRequest(
        Long parentId,
        @NotBlank @Size(max = 128) String nodeCode,
        @NotBlank @Size(max = 32) String nodeType,
        @Size(max = 255) String title,
        String direction,
        String material,
        Integer sortOrder,
        List<@Valid QuestionNodeOptionRequest> sharedOptions,
        List<@Valid QuestionAttachmentRequest> attachments
) {
}
