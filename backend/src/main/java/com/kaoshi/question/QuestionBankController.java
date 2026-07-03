package com.kaoshi.question;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.common.page.PageRequest;
import com.kaoshi.common.page.PageResponse;
import com.kaoshi.question.domain.QuestionCategory;
import com.kaoshi.question.dto.QuestionBankResponse;
import com.kaoshi.question.dto.QuestionBankSaveRequest;
import com.kaoshi.question.dto.QuestionCategorySaveRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/question-banks")
@PreAuthorize("hasAuthority('system:admin')")
public class QuestionBankController {
    private final QuestionBankService bankService;

    public QuestionBankController(QuestionBankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping
    public ApiResponse<PageResponse<QuestionBankResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(bankService.page(new PageRequest(page, size, keyword)));
    }

    @GetMapping("/categories")
    public ApiResponse<List<QuestionCategory>> categories() {
        return ApiResponse.ok(bankService.categories());
    }

    @PostMapping("/categories")
    public ApiResponse<QuestionCategory> createCategory(@Valid @RequestBody QuestionCategorySaveRequest request) {
        return ApiResponse.ok(bankService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<QuestionCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody QuestionCategorySaveRequest request
    ) {
        return ApiResponse.ok(bankService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        bankService.deleteCategory(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionBankResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(bankService.detail(id));
    }

    @PostMapping
    public ApiResponse<QuestionBankResponse> create(@Valid @RequestBody QuestionBankSaveRequest request) {
        return ApiResponse.ok(bankService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<QuestionBankResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionBankSaveRequest request
    ) {
        return ApiResponse.ok(bankService.update(id, request));
    }
}

