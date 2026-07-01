package com.kaoshi.admin.role;

import com.kaoshi.admin.role.dto.AdminRoleResponse;
import com.kaoshi.admin.role.dto.RoleSaveRequest;
import com.kaoshi.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasAuthority('system:admin')")
public class AdminRoleController {
    private final AdminRoleService roleService;

    public AdminRoleController(AdminRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<AdminRoleResponse>> list() {
        return ApiResponse.ok(roleService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminRoleResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(roleService.detail(id));
    }

    @PostMapping
    public ApiResponse<AdminRoleResponse> create(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminRoleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RoleSaveRequest request
    ) {
        return ApiResponse.ok(roleService.update(id, request));
    }
}

