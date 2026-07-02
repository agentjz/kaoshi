package com.kaoshi.system.department;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.common.excel.ExcelImportResult;
import com.kaoshi.system.department.dto.DepartmentResponse;
import com.kaoshi.system.department.dto.DepartmentSaveRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
@PreAuthorize("hasAuthority('system:admin')")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> tree() {
        return ApiResponse.ok(departmentService.tree());
    }

    @PostMapping
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentSaveRequest request) {
        return ApiResponse.ok(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentSaveRequest request) {
        return ApiResponse.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/import-template")
    public ResponseEntity<byte[]> importTemplate() {
        return departmentService.template();
    }

    @PostMapping("/import")
    public ApiResponse<ExcelImportResult> importExcel(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(departmentService.importExcel(file));
    }
}
