package com.kaoshi.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExternalIntegrationRequest(
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Size(max = 64) String integrationType,
        @NotBlank @Size(max = 1000) String endpointUrl,
        @Size(max = 128) String secretMask,
        Boolean enabled
) {
}
