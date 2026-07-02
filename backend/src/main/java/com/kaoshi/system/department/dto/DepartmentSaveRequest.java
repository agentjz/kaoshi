package com.kaoshi.system.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentSaveRequest(
        Long parentId,
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Size(max = 64) String code,
        @Size(max = 500) String description,
        @NotBlank String status
) {
}
