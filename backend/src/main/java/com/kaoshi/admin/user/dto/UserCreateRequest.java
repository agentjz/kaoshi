package com.kaoshi.admin.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateRequest(
        Long departmentId,
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(max = 64) String displayName,
        @NotBlank @Size(min = 6, max = 128) String password,
        @NotEmpty List<Long> roleIds
) {
}

