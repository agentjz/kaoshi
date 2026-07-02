package com.kaoshi.admin.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserUpdateRequest(
        Long departmentId,
        @NotBlank @Size(max = 64) String displayName,
        @Size(min = 6, max = 128) String password,
        @NotEmpty List<Long> roleIds
) {
}

