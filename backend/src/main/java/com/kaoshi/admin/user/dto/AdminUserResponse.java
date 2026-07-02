package com.kaoshi.admin.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        Long departmentId,
        String departmentName,
        String username,
        String displayName,
        String status,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

