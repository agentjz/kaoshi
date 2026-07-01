package com.kaoshi.admin.permission.dto;

public record AdminPermissionResponse(
        Long id,
        String code,
        String name,
        String description
) {
}

