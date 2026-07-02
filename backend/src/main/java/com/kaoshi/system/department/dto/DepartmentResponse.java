package com.kaoshi.system.department.dto;

import java.util.List;

public record DepartmentResponse(
        Long id,
        Long parentId,
        String name,
        String code,
        String description,
        String status,
        List<DepartmentResponse> children
) {
}
