package com.kaoshi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(max = 64) String displayName,
        @NotBlank @Size(min = 6, max = 64) String password,
        @NotBlank @Size(min = 6, max = 64) String confirmPassword,
        @Size(min = 6, max = 6) String verificationCode
) {
}
