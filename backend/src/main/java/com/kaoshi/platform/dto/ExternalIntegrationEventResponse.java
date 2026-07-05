package com.kaoshi.platform.dto;

import java.time.LocalDateTime;

public record ExternalIntegrationEventResponse(
        Long id,
        Long integrationId,
        String eventType,
        String status,
        String payloadSummary,
        String errorMessage,
        LocalDateTime createdAt
) {
}
