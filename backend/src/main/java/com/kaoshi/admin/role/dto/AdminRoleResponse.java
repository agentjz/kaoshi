package com.kaoshi.admin.role.dto;

import com.kaoshi.admin.menu.dto.AdminMenuResponse;
import com.kaoshi.admin.permission.dto.AdminPermissionResponse;

import java.util.List;

public record AdminRoleResponse(
        Long id,
        String code,
        String name,
        String description,
        List<AdminPermissionResponse> permissions,
        List<AdminMenuResponse> menus
) {
}
