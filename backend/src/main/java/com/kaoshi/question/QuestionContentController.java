package com.kaoshi.question;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.common.excel.ExcelImportResult;
import com.kaoshi.question.dto.QuestionContentNodeResponse;
import com.kaoshi.question.dto.QuestionContentTreeResponse;
import com.kaoshi.question.dto.QuestionBankPackageImportResponse;
import com.kaoshi.question.dto.QuestionNodeSaveRequest;
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

@RestController
@RequestMapping("/api/admin/question-banks")
@PreAuthorize("hasAuthority('system:admin')")
public class QuestionContentController {
    private final QuestionContentService contentService;
    private final QuestionGroupExcelImportService groupExcelImportService;
    private final QuestionBankPackageService packageService;

    public QuestionContentController(
            QuestionContentService contentService,
            QuestionGroupExcelImportService groupExcelImportService,
            QuestionBankPackageService packageService
    ) {
        this.contentService = contentService;
        this.groupExcelImportService = groupExcelImportService;
        this.packageService = packageService;
    }

    @GetMapping("/{bankId}/content-tree")
    public ApiResponse<QuestionContentTreeResponse> tree(@PathVariable Long bankId) {
        return ApiResponse.ok(contentService.tree(bankId));
    }

    @PostMapping("/{bankId}/nodes")
    public ApiResponse<QuestionContentNodeResponse> createNode(
            @PathVariable Long bankId,
            @Valid @RequestBody QuestionNodeSaveRequest request
    ) {
        return ApiResponse.ok(contentService.createNode(bankId, request));
    }

    @PutMapping("/nodes/{nodeId}")
    public ApiResponse<QuestionContentNodeResponse> updateNode(
            @PathVariable Long nodeId,
            @Valid @RequestBody QuestionNodeSaveRequest request
    ) {
        return ApiResponse.ok(contentService.updateNode(nodeId, request));
    }

    @DeleteMapping("/nodes/{nodeId}")
    public ApiResponse<Void> deleteNode(@PathVariable Long nodeId) {
        contentService.deleteNode(nodeId);
        return ApiResponse.ok();
    }

    @PostMapping("/nodes/{nodeId}/questions/import")
    public ApiResponse<ExcelImportResult> importQuestionsToGroup(
            @PathVariable Long nodeId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(groupExcelImportService.importToGroup(nodeId, file));
    }

    @GetMapping("/{bankId}/package")
    public ResponseEntity<byte[]> exportPackage(@PathVariable Long bankId) {
        return packageService.exportPackage(bankId);
    }

    @PostMapping("/package/import")
    public ApiResponse<QuestionBankPackageImportResponse> importPackage(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(packageService.importPackage(file));
    }
}
