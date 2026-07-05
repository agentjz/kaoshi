package com.kaoshi.auth.dto;

public record RegisterResponse(
        Long userId,
        String username,
        String email,
        boolean emailVerified,
        String approvalStatus,
        String message
) {
}
