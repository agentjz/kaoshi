package com.kaoshi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) String code,
        @NotBlank @Size(min = 6, max = 64) String newPassword,
        @NotBlank @Size(min = 6, max = 64) String confirmPassword
) {
}
