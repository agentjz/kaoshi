package com.kaoshi.exam.governance;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.exam.governance.dto.ExamAllowanceRequest;
import com.kaoshi.exam.governance.dto.ExamAttemptEventResponse;
import com.kaoshi.exam.governance.dto.ExamParticipantResponse;
import com.kaoshi.exam.governance.dto.ExamParticipantSaveRequest;
import com.kaoshi.exam.governance.dto.ExamReportResponse;
import com.kaoshi.exam.governance.dto.ExamReviewRecheckRequest;
import com.kaoshi.exam.governance.dto.ExamReviewRecheckResponse;
import com.kaoshi.exam.governance.dto.ExamReviewRubricRequest;
import com.kaoshi.exam.governance.dto.ExamReviewRubricResponse;
import com.kaoshi.exam.governance.dto.ExamReviewTaskResponse;
import com.kaoshi.exam.governance.dto.ExamReviewTaskUpdateRequest;
import com.kaoshi.exam.governance.dto.ExamResultPolicyRequest;
import com.kaoshi.exam.governance.dto.ExamResultPolicyResponse;
import com.kaoshi.exam.governance.dto.ExamRetakeRequest;
import com.kaoshi.exam.governance.dto.ExamSecurityEventResponse;
import com.kaoshi.exam.governance.dto.ExamSecurityPolicyRequest;
import com.kaoshi.exam.governance.dto.ExamSecurityPolicyResponse;
import com.kaoshi.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exams/{examId}/governance")
@PreAuthorize("hasAuthority('system:admin')")
public class AdminExamGovernanceController {
    private final ExamGovernanceService governanceService;

    public AdminExamGovernanceController(ExamGovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @GetMapping("/participants")
    public ApiResponse<List<ExamParticipantResponse>> participants(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.participants(examId));
    }

    @PutMapping("/participants")
    public ApiResponse<List<ExamParticipantResponse>> replaceParticipants(
            @PathVariable Long examId,
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ExamParticipantSaveRequest request
    ) {
        return ApiResponse.ok(governanceService.replaceParticipants(examId, user.id(), request.userIds()));
    }

    @PutMapping("/participants/{userId}/allowance")
    public ApiResponse<ExamParticipantResponse> updateAllowance(
            @PathVariable Long examId,
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ExamAllowanceRequest request
    ) {
        return ApiResponse.ok(governanceService.updateAllowance(examId, userId, user.id(), request));
    }

    @PostMapping("/participants/{userId}/retake")
    public ApiResponse<ExamParticipantResponse> grantRetake(
            @PathVariable Long examId,
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthUser user,
            @RequestBody(required = false) ExamRetakeRequest request
    ) {
        return ApiResponse.ok(governanceService.grantRetake(examId, userId, user.id(), request == null ? null : request.reason()));
    }

    @GetMapping("/result-policy")
    public ApiResponse<ExamResultPolicyResponse> resultPolicy(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.resultPolicy(examId));
    }

    @PutMapping("/result-policy")
    public ApiResponse<ExamResultPolicyResponse> updateResultPolicy(
            @PathVariable Long examId,
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ExamResultPolicyRequest request
    ) {
        return ApiResponse.ok(governanceService.updateResultPolicy(examId, user.id(), request));
    }

    @GetMapping("/report")
    public ApiResponse<ExamReportResponse> report(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.report(examId));
    }

    @GetMapping("/events")
    public ApiResponse<List<ExamAttemptEventResponse>> events(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.events(examId));
    }

    @GetMapping("/security-policy")
    public ApiResponse<ExamSecurityPolicyResponse> securityPolicy(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.securityPolicy(examId));
    }

    @PutMapping("/security-policy")
    public ApiResponse<ExamSecurityPolicyResponse> updateSecurityPolicy(
            @PathVariable Long examId,
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ExamSecurityPolicyRequest request
    ) {
        return ApiResponse.ok(governanceService.updateSecurityPolicy(examId, user.id(), request));
    }

    @GetMapping("/security-events")
    public ApiResponse<List<ExamSecurityEventResponse>> securityEvents(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.securityEvents(examId));
    }

    @GetMapping("/rubrics")
    public ApiResponse<List<ExamReviewRubricResponse>> rubrics(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.rubrics(examId));
    }

    @PutMapping("/rubrics")
    public ApiResponse<List<ExamReviewRubricResponse>> replaceRubrics(
            @PathVariable Long examId,
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody List<ExamReviewRubricRequest> request
    ) {
        return ApiResponse.ok(governanceService.replaceRubrics(examId, user.id(), request));
    }

    @GetMapping("/review-tasks")
    public ApiResponse<List<ExamReviewTaskResponse>> reviewTasks(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.reviewTasks(examId));
    }

    @PostMapping("/review-tasks/generate")
    public ApiResponse<List<ExamReviewTaskResponse>> generateReviewTasks(@PathVariable Long examId, @AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(governanceService.generateReviewTasks(examId, user.id()));
    }

    @PostMapping("/review-tasks/{taskId}/claim")
    public ApiResponse<List<ExamReviewTaskResponse>> claimReviewTask(@PathVariable Long taskId, @AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(governanceService.claimReviewTask(taskId, user.id()));
    }

    @PutMapping("/review-tasks/{taskId}")
    public ApiResponse<List<ExamReviewTaskResponse>> updateReviewTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ExamReviewTaskUpdateRequest request
    ) {
        return ApiResponse.ok(governanceService.updateReviewTask(taskId, user.id(), request));
    }

    @GetMapping("/rechecks")
    public ApiResponse<List<ExamReviewRecheckResponse>> rechecks(@PathVariable Long examId) {
        return ApiResponse.ok(governanceService.rechecks(examId));
    }

    @PostMapping("/review-tasks/{taskId}/recheck")
    public ApiResponse<List<ExamReviewRecheckResponse>> requestRecheck(
            @PathVariable Long taskId,
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ExamReviewRecheckRequest request
    ) {
        return ApiResponse.ok(governanceService.requestRecheck(taskId, user.id(), request));
    }

    @PutMapping("/rechecks/{recheckId}")
    public ApiResponse<List<ExamReviewRecheckResponse>> updateRecheck(
            @PathVariable Long examId,
            @PathVariable Long recheckId,
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ExamReviewRecheckRequest request
    ) {
        return ApiResponse.ok(governanceService.updateRecheck(examId, recheckId, user.id(), request));
    }
}
