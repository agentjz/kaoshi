package com.kaoshi.paper;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.common.page.PageRequest;
import com.kaoshi.common.page.PageResponse;
import com.kaoshi.paper.dto.PaperResponse;
import com.kaoshi.paper.dto.PaperSaveRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/papers")
@PreAuthorize("hasAuthority('system:admin')")
public class PaperController {
    private final PaperService paperService;

    public PaperController(PaperService paperService) {
        this.paperService = paperService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<?>> categories() {
        return ApiResponse.ok(paperService.categories());
    }

    @GetMapping
    public ApiResponse<PageResponse<PaperResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(paperService.page(new PageRequest(page, size, keyword)));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaperResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(paperService.detail(id));
    }

    @PostMapping
    public ApiResponse<PaperResponse> create(@Valid @RequestBody PaperSaveRequest request) {
        return ApiResponse.ok(paperService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PaperResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PaperSaveRequest request
    ) {
        return ApiResponse.ok(paperService.update(id, request));
    }
}

