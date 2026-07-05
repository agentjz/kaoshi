package com.kaoshi.auth.dto;

import java.time.LocalDateTime;

public record VerificationCodeResponse(
        String email,
        String purpose,
        LocalDateTime expiresAt,
        String debugCode
) {
}
