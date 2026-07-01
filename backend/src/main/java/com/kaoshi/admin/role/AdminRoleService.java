package com.kaoshi.admin.role;

import com.kaoshi.admin.menu.domain.Menu;
import com.kaoshi.admin.menu.dto.AdminMenuResponse;
import com.kaoshi.admin.menu.mapper.AdminMenuMapper;
import com.kaoshi.admin.permission.dto.AdminPermissionResponse;
import com.kaoshi.admin.permission.mapper.AdminPermissionMapper;
import com.kaoshi.admin.role.dto.AdminRoleResponse;
import com.kaoshi.admin.role.dto.RoleSaveRequest;
import com.kaoshi.admin.role.mapper.AdminRoleMapper;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.user.domain.Permission;
import com.kaoshi.user.domain.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminRoleService {
    private final AdminRoleMapper roleMapper;
    private final AdminPermissionMapper permissionMapper;
    private final AdminMenuMapper menuMapper;

    public AdminRoleService(
            AdminRoleMapper roleMapper,
            AdminPermissionMapper permissionMapper,
            AdminMenuMapper menuMapper
    ) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.menuMapper = menuMapper;
    }

    public List<AdminRoleResponse> list() {
        return roleMapper.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminRoleResponse detail(Long id) {
        return toResponse(findRole(id));
    }

    @Transactional
    public AdminRoleResponse create(RoleSaveRequest request) {
        if (roleMapper.countByCode(request.code()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        ensurePermissionsExist(request.permissionIds());
        ensureMenusExist(request.menuIds());

        Role role = new Role();
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        roleMapper.insertRole(role);
        replaceRelations(role.getId(), request.permissionIds(), request.menuIds());
        return detail(role.getId());
    }

    @Transactional
    public AdminRoleResponse update(Long id, RoleSaveRequest request) {
        Role role = findRole(id);
        if (roleMapper.countByCodeExceptId(request.code(), id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        ensurePermissionsExist(request.permissionIds());
        ensureMenusExist(request.menuIds());

        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        roleMapper.updateRole(role);
        replaceRelations(id, request.permissionIds(), request.menuIds());
        return detail(id);
    }

    private Role findRole(Long id) {
        Role role = roleMapper.findById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private void replaceRelations(Long roleId, List<Long> permissionIds, List<Long> menuIds) {
        roleMapper.deleteRolePermissions(roleId);
        new HashSet<>(permissionIds).forEach(permissionId -> roleMapper.insertRolePermission(roleId, permissionId));
        roleMapper.deleteRoleMenus(roleId);
        new HashSet<>(menuIds).forEach(menuId -> roleMapper.insertRoleMenu(roleId, menuId));
    }

    private void ensurePermissionsExist(List<Long> permissionIds) {
        Set<Long> uniquePermissionIds = new HashSet<>(permissionIds);
        if (permissionMapper.countByIds(uniquePermissionIds) != uniquePermissionIds.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "权限不存在");
        }
    }

    private void ensureMenusExist(List<Long> menuIds) {
        Set<Long> uniqueMenuIds = new HashSet<>(menuIds);
        if (menuMapper.countByIds(uniqueMenuIds) != uniqueMenuIds.size()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "菜单不存在");
        }
    }

    private AdminRoleResponse toResponse(Role role) {
        return new AdminRoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                roleMapper.findPermissions(role.getId()).stream().map(this::toPermissionResponse).toList(),
                roleMapper.findMenus(role.getId()).stream().map(this::toMenuResponse).toList()
        );
    }

    private AdminPermissionResponse toPermissionResponse(Permission permission) {
        return new AdminPermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription()
        );
    }

    private AdminMenuResponse toMenuResponse(Menu menu) {
        return new AdminMenuResponse(
                menu.getId(),
                menu.getCode(),
                menu.getTitle(),
                menu.getPath(),
                menu.getParentId(),
                menu.getSortOrder(),
                menu.getIcon()
        );
    }
}
