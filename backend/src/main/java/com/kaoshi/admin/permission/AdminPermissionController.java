package com.kaoshi.admin.permission;

import com.kaoshi.admin.permission.dto.AdminPermissionResponse;
import com.kaoshi.admin.permission.mapper.AdminPermissionMapper;
import com.kaoshi.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasAuthority('system:admin')")
public class AdminPermissionController {
    private final AdminPermissionMapper permissionMapper;

    public AdminPermissionController(AdminPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @GetMapping
    public ApiResponse<List<AdminPermissionResponse>> list() {
        return ApiResponse.ok(permissionMapper.findAll().stream()
                .map(permission -> new AdminPermissionResponse(
                        permission.getId(),
                        permission.getCode(),
                        permission.getName(),
                        permission.getDescription()
                ))
                .toList());
    }
}

