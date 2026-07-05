package com.kaoshi.auth.dto;

import java.util.List;

public record RegistrationSettingsResponse(
        boolean selfRegistrationEnabled,
        boolean emailVerificationRequired,
        boolean adminApprovalRequired,
        String defaultRoleCode,
        Long defaultDepartmentId,
        List<String> allowedEmailDomains,
        String termsText
) {
}
