package com.kaoshi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationCodeRequest(
        @NotBlank @Email String email,
        @NotBlank String purpose
) {
}
