package com.kaoshi.auth.dto;

import java.time.LocalDateTime;

public record RegistrationRequestResponse(
        Long userId,
        String username,
        String displayName,
        String email,
        String status,
        String approvalStatus,
        LocalDateTime registeredAt
) {
}
