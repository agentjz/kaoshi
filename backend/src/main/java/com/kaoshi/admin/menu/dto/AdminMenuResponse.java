package com.kaoshi.admin.menu.dto;

public record AdminMenuResponse(
        Long id,
        String code,
        String title,
        String path,
        Long parentId,
        Integer sortOrder,
        String icon
) {
}

