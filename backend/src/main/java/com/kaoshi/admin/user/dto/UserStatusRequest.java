package com.kaoshi.admin.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserStatusRequest(@NotBlank String status) {
}

