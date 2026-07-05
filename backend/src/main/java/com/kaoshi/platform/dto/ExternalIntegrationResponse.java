package com.kaoshi.platform.dto;

import java.time.LocalDateTime;

public record ExternalIntegrationResponse(
        Long id,
        String name,
        String integrationType,
        String endpointUrl,
        String secretMask,
        Boolean enabled,
        LocalDateTime updatedAt
) {
}
