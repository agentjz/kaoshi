package com.kaoshi.admin.menu;

import com.kaoshi.admin.menu.dto.AdminMenuResponse;
import com.kaoshi.admin.menu.mapper.AdminMenuMapper;
import com.kaoshi.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
@PreAuthorize("hasAuthority('system:admin')")
public class AdminMenuController {
    private final AdminMenuMapper menuMapper;

    public AdminMenuController(AdminMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @GetMapping
    public ApiResponse<List<AdminMenuResponse>> list() {
        return ApiResponse.ok(menuMapper.findAll().stream()
                .map(menu -> new AdminMenuResponse(
                        menu.getId(),
                        menu.getCode(),
                        menu.getTitle(),
                        menu.getPath(),
                        menu.getParentId(),
                        menu.getSortOrder(),
                        menu.getIcon()
                ))
                .toList());
    }
}

