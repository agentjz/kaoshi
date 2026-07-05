package com.kaoshi.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegistrationSettingsRequest(
        boolean selfRegistrationEnabled,
        boolean emailVerificationRequired,
        boolean adminApprovalRequired,
        @NotBlank String defaultRoleCode,
        Long defaultDepartmentId,
        List<@Size(max = 255) String> allowedEmailDomains,
        @Size(max = 1000) String termsText
) {
}
