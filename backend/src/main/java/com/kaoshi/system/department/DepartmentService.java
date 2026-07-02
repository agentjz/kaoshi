package com.kaoshi.system.department;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.common.excel.ExcelImportResult;
import com.kaoshi.common.excel.ExcelWorkbooks;
import com.kaoshi.system.department.domain.Department;
import com.kaoshi.system.department.dto.DepartmentResponse;
import com.kaoshi.system.department.dto.DepartmentSaveRequest;
import com.kaoshi.system.department.mapper.DepartmentMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public List<DepartmentResponse> tree() {
        List<Department> departments = departmentMapper.findAll();
        Map<Long, List<Department>> children = new HashMap<>();
        for (Department department : departments) {
            children.computeIfAbsent(department.getParentId(), ignored -> new ArrayList<>()).add(department);
        }
        return children.getOrDefault(null, List.of()).stream()
                .map(department -> toResponse(department, children))
                .toList();
    }

    @Transactional
    public DepartmentResponse create(DepartmentSaveRequest request) {
        validateSave(null, request);
        Department department = new Department();
        fill(department, request);
        departmentMapper.insertDepartment(department);
        return toResponse(departmentMapper.findById(department.getId()), Map.of());
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentSaveRequest request) {
        Department department = findDepartment(id);
        validateSave(id, request);
        fill(department, request);
        departmentMapper.updateDepartment(department);
        return toResponse(departmentMapper.findById(id), Map.of());
    }

    @Transactional
    public void delete(Long id) {
        findDepartment(id);
        if (departmentMapper.countChildren(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "部门存在下级，不能删除");
        }
        departmentMapper.deleteDepartment(id);
    }

    public ResponseEntity<byte[]> template() {
        List<List<String>> departmentRows = departmentMapper.findAll().stream()
                .map(department -> List.of(
                        department.getCode(),
                        department.getName(),
                        department.getParentId() == null ? "" : departmentMapper.findById(department.getParentId()).getCode(),
                        department.getStatus()
                ))
                .toList();
        return ExcelWorkbooks.template(
                "部门导入模板.xlsx",
                List.of(
                        new ExcelWorkbooks.SheetData(
                                "导入模板",
                                List.of("部门编码", "部门名称", "上级部门编码", "状态", "说明"),
                                List.of(
                                        List.of("ENGLISH_GROUP", "英语教研组", "DEFAULT", "ACTIVE", "负责英语考试"),
                                        List.of("LISTENING_TEAM", "听力组", "ENGLISH_GROUP", "ACTIVE", "负责听力材料")
                                )
                        ),
                        new ExcelWorkbooks.SheetData(
                                "现有部门",
                                List.of("部门编码", "部门名称", "上级部门编码", "状态"),
                                departmentRows
                        )
                )
        );
    }

    @Transactional
    public ExcelImportResult importExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelWorkbooks.text(row, 0).isBlank()) {
                    continue;
                }
                try {
                    DepartmentSaveRequest request = rowToRequest(row);
                    Department existing = departmentMapper.findByCode(request.code());
                    if (existing == null) {
                        create(request);
                    } else {
                        update(existing.getId(), request);
                    }
                    success++;
                } catch (RuntimeException exception) {
                    errors.add("第 " + (rowIndex + 1) + " 行：" + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "读取 Excel 失败");
        }
        return new ExcelImportResult(success, errors.size(), errors);
    }

    private DepartmentSaveRequest rowToRequest(Row row) {
        String code = ExcelWorkbooks.text(row, 0).trim();
        String name = ExcelWorkbooks.text(row, 1).trim();
        String parentCode = ExcelWorkbooks.text(row, 2).trim();
        String status = normalizeStatus(ExcelWorkbooks.text(row, 3).trim());
        String description = ExcelWorkbooks.text(row, 4).trim();
        Long parentId = null;
        if (!parentCode.isBlank()) {
            Department parent = departmentMapper.findByCode(parentCode);
            if (parent == null) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上级部门不存在：" + parentCode);
            }
            parentId = parent.getId();
        }
        return new DepartmentSaveRequest(parentId, name, code, description, status);
    }

    private void validateSave(Long id, DepartmentSaveRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "部门名称不能为空");
        }
        if (request.code() == null || request.code().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "部门编码不能为空");
        }
        if (!List.of("ACTIVE", "DISABLED").contains(request.status())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "部门状态不合法");
        }
        if (request.parentId() != null) {
            if (id != null && id.equals(request.parentId())) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上级部门不能选择自己");
            }
            findDepartment(request.parentId());
            if (id != null && isDescendant(request.parentId(), id)) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上级部门不能选择自己的下级部门");
            }
        }
        int duplicate = id == null ? departmentMapper.countByCode(request.code()) : departmentMapper.countByCodeExceptId(request.code(), id);
        if (duplicate > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "部门编码已存在");
        }
    }

    private boolean isDescendant(Long candidateParentId, Long departmentId) {
        Map<Long, Department> departments = new HashMap<>();
        for (Department department : departmentMapper.findAll()) {
            departments.put(department.getId(), department);
        }
        Long currentId = candidateParentId;
        while (currentId != null) {
            if (departmentId.equals(currentId)) {
                return true;
            }
            Department current = departments.get(currentId);
            currentId = current == null ? null : current.getParentId();
        }
        return false;
    }

    private String normalizeStatus(String value) {
        if (value.isBlank() || "启用".equals(value)) {
            return "ACTIVE";
        }
        if ("禁用".equals(value)) {
            return "DISABLED";
        }
        return value.toUpperCase();
    }

    private Department findDepartment(Long id) {
        Department department = departmentMapper.findById(id);
        if (department == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }
        return department;
    }

    private void fill(Department department, DepartmentSaveRequest request) {
        department.setParentId(request.parentId());
        department.setName(request.name());
        department.setCode(request.code());
        department.setDescription(request.description());
        department.setStatus(request.status());
    }

    private DepartmentResponse toResponse(Department department, Map<Long, List<Department>> children) {
        return new DepartmentResponse(
                department.getId(),
                department.getParentId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getStatus(),
                children.getOrDefault(department.getId(), List.of()).stream()
                        .map(child -> toResponse(child, children))
                        .toList()
        );
    }
}
