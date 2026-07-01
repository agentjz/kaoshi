package com.kaoshi.admin.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RoleSaveRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 64) String name,
        @Size(max = 255) String description,
        @NotEmpty List<Long> permissionIds,
        @NotEmpty List<Long> menuIds
) {
}

