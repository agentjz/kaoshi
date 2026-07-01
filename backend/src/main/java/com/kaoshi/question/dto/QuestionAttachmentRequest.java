package com.kaoshi.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionAttachmentRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 1000) String fileUrl,
        @NotBlank @Size(max = 32) String mediaType
) {
}

