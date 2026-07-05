package com.kaoshi.exam.governance;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.exam.domain.Exam;
import com.kaoshi.exam.governance.dto.ExamAllowanceRequest;
import com.kaoshi.exam.governance.dto.ExamAttemptEventResponse;
import com.kaoshi.exam.governance.dto.ExamParticipantResponse;
import com.kaoshi.exam.governance.dto.ExamReportResponse;
import com.kaoshi.exam.governance.dto.ExamReviewRecheckRequest;
import com.kaoshi.exam.governance.dto.ExamReviewRecheckResponse;
import com.kaoshi.exam.governance.dto.ExamReviewRubricRequest;
import com.kaoshi.exam.governance.dto.ExamReviewRubricResponse;
import com.kaoshi.exam.governance.dto.ExamReviewTaskResponse;
import com.kaoshi.exam.governance.dto.ExamReviewTaskUpdateRequest;
import com.kaoshi.exam.governance.dto.ExamResultPolicyRequest;
import com.kaoshi.exam.governance.dto.ExamResultPolicyResponse;
import com.kaoshi.exam.governance.dto.ExamSecurityEventRequest;
import com.kaoshi.exam.governance.dto.ExamSecurityEventResponse;
import com.kaoshi.exam.governance.dto.ExamSecurityPolicyRequest;
import com.kaoshi.exam.governance.dto.ExamSecurityPolicyResponse;
import com.kaoshi.platform.PlatformGovernanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.kaoshi.exam.ExamRowValues.booleanValue;
import static com.kaoshi.exam.ExamRowValues.dateTimeValue;
import static com.kaoshi.exam.ExamRowValues.dateTimeValueOrNull;
import static com.kaoshi.exam.ExamRowValues.decimalValue;
import static com.kaoshi.exam.ExamRowValues.intValue;
import static com.kaoshi.exam.ExamRowValues.longValue;
import static com.kaoshi.exam.ExamRowValues.stringValue;
import static com.kaoshi.exam.ExamRowValues.value;

@Service
public class ExamGovernanceService {
    private final ExamGovernanceMapper mapper;
    private final PlatformGovernanceService platformGovernanceService;

    public ExamGovernanceService(ExamGovernanceMapper mapper, PlatformGovernanceService platformGovernanceService) {
        this.mapper = mapper;
        this.platformGovernanceService = platformGovernanceService;
    }

    public void ensureExamOpenToUser(Exam exam, Long userId) {
        if (mapper.countParticipants(exam.getId()) > 0) {
            if (mapper.countAssignedParticipant(exam.getId(), userId) == 0) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "不在本场考试考生名册内");
            }
            return;
        }
        if ("PUBLIC".equals(exam.getOpenType())) {
            return;
        }
        Long departmentId = mapper.findUserDepartmentId(userId);
        if (departmentId == null || !mapper.findExamDepartmentIds(exam.getId()).contains(departmentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有该考试权限");
        }
    }

    public Integer effectiveAttemptLimit(Exam exam, Long userId) {
        Integer extraAttempts = intValueOrZero(valueOrNull(mapper.findAllowance(exam.getId(), userId), "extraAttempts"));
        if (exam.getAttemptLimit() == null) {
            return null;
        }
        return exam.getAttemptLimit() + extraAttempts;
    }

    public Integer effectiveDurationMinutes(Exam exam, Long userId) {
        Integer extraMinutes = intValueOrZero(valueOrNull(mapper.findAllowance(exam.getId(), userId), "extraMinutes"));
        return exam.getDurationMinutes() + extraMinutes;
    }

    public boolean isResultVisibleToStudent(Long examId) {
        Map<String, Object> row = mapper.findResultPolicy(examId);
        if (row == null) {
            return true;
        }
        if (!Boolean.TRUE.equals(booleanValue(value(row, "visibleToStudents")))) {
            return false;
        }
        LocalDateTime releaseTime = dateTimeValueOrNull(value(row, "releaseTime"));
        return releaseTime == null || !LocalDateTime.now().isBefore(releaseTime);
    }

    public boolean shouldShowAnswers(Long examId) {
        Map<String, Object> row = mapper.findResultPolicy(examId);
        return row == null || Boolean.TRUE.equals(booleanValue(value(row, "showAnswers")));
    }

    public boolean shouldShowAnalysis(Long examId) {
        Map<String, Object> row = mapper.findResultPolicy(examId);
        return row == null || Boolean.TRUE.equals(booleanValue(value(row, "showAnalysis")));
    }

    public List<ExamParticipantResponse> participants(Long examId) {
        ensureExamExists(examId);
        return mapper.findParticipants(examId).stream().map(this::toParticipant).toList();
    }

    @Transactional
    public List<ExamParticipantResponse> replaceParticipants(Long examId, Long actorUserId, List<Long> userIds) {
        ensureExamExists(examId);
        mapper.deleteParticipants(examId);
        for (Long userId : userIds.stream().distinct().toList()) {
            ensureActiveUser(userId);
            mapper.upsertParticipant(examId, userId, actorUserId);
        }
        mapper.insertEvent(examId, null, null, actorUserId, "EXAM_PARTICIPANTS_REPLACED", "更新考生名册：" + userIds.size() + " 人");
        return participants(examId);
    }

    @Transactional
    public ExamParticipantResponse updateAllowance(Long examId, Long userId, Long actorUserId, ExamAllowanceRequest request) {
        ensureExamExists(examId);
        ensureActiveUser(userId);
        mapper.upsertParticipant(examId, userId, actorUserId);
        int extraMinutes = request.extraMinutes() == null ? 0 : request.extraMinutes();
        int extraAttempts = request.extraAttempts() == null ? 0 : request.extraAttempts();
        mapper.upsertAllowance(examId, userId, extraMinutes, extraAttempts, trim(request.reason()), actorUserId);
        mapper.insertEvent(examId, null, userId, actorUserId, "EXAM_ALLOWANCE_UPDATED", trim(request.reason()));
        return participants(examId).stream()
                .filter(participant -> userId.equals(participant.userId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "考生不存在"));
    }

    @Transactional
    public ExamParticipantResponse grantRetake(Long examId, Long userId, Long actorUserId, String reason) {
        ensureExamExists(examId);
        ensureActiveUser(userId);
        mapper.upsertParticipant(examId, userId, actorUserId);
        int updated = mapper.incrementExtraAttempt(examId, userId, trim(reason), actorUserId);
        if (updated == 0) {
            mapper.upsertAllowance(examId, userId, 0, 1, trim(reason), actorUserId);
        }
        mapper.insertEvent(examId, null, userId, actorUserId, "EXAM_RETAKE_GRANTED", trim(reason));
        return participants(examId).stream()
                .filter(participant -> userId.equals(participant.userId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "考生不存在"));
    }

    public ExamResultPolicyResponse resultPolicy(Long examId) {
        ensureExamExists(examId);
        Map<String, Object> row = mapper.findResultPolicy(examId);
        if (row == null) {
            return new ExamResultPolicyResponse(examId, true, true, true, null, null);
        }
        return toPolicy(row);
    }

    @Transactional
    public ExamResultPolicyResponse updateResultPolicy(Long examId, Long actorUserId, ExamResultPolicyRequest request) {
        ensureExamExists(examId);
        boolean visible = request.visibleToStudents() == null || request.visibleToStudents();
        boolean showAnswers = request.showAnswers() == null || request.showAnswers();
        boolean showAnalysis = request.showAnalysis() == null || request.showAnalysis();
        mapper.upsertResultPolicy(examId, visible, showAnswers, showAnalysis, request.releaseTime(), actorUserId);
        mapper.insertEvent(examId, null, null, actorUserId, "EXAM_RESULT_POLICY_UPDATED", null);
        return resultPolicy(examId);
    }

    public ExamReportResponse report(Long examId) {
        ensureExamExists(examId);
        int rosterCount = mapper.countParticipants(examId);
        int participantCount = rosterCount > 0 ? rosterCount : mapper.countSubmitted(examId);
        int submittedCount = mapper.countSubmitted(examId);
        int passedCount = mapper.countPassed(examId);
        BigDecimal passRate = submittedCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(passedCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(submittedCount), 2, RoundingMode.HALF_UP);
        return new ExamReportResponse(
                examId,
                participantCount,
                submittedCount,
                mapper.countPendingReview(examId),
                mapper.averageScore(examId).setScale(2, RoundingMode.HALF_UP),
                mapper.maxScore(examId).setScale(2, RoundingMode.HALF_UP),
                mapper.minScore(examId).setScale(2, RoundingMode.HALF_UP),
                passRate
        );
    }

    public List<ExamAttemptEventResponse> events(Long examId) {
        ensureExamExists(examId);
        return mapper.findEvents(examId).stream().map(this::toEvent).toList();
    }

    public ExamSecurityPolicyResponse securityPolicy(Long examId) {
        ensureExamExists(examId);
        Map<String, Object> row = mapper.findSecurityPolicy(examId);
        if (row == null) {
            return new ExamSecurityPolicyResponse(examId, false, true, true, 3, false, null);
        }
        return toSecurityPolicy(row);
    }

    @Transactional
    public ExamSecurityPolicyResponse updateSecurityPolicy(Long examId, Long actorUserId, ExamSecurityPolicyRequest request) {
        ensureExamExists(examId);
        int maxFocusLossCount = request.maxFocusLossCount() == null ? 3 : Math.max(0, request.maxFocusLossCount());
        mapper.upsertSecurityPolicy(
                examId,
                Boolean.TRUE.equals(request.requireFullscreen()),
                request.forbidCopyPaste() == null || request.forbidCopyPaste(),
                request.trackFocusLoss() == null || request.trackFocusLoss(),
                maxFocusLossCount,
                Boolean.TRUE.equals(request.deviceCheckRequired()),
                actorUserId
        );
        mapper.insertEvent(examId, null, null, actorUserId, "EXAM_SECURITY_POLICY_UPDATED", null);
        platformGovernanceService.notifyAll("考试安全策略已更新", "考试 " + examId + " 的安全策略已更新。", "SECURITY");
        return securityPolicy(examId);
    }

    @Transactional
    public void recordSecurityEvent(Long examId, Long userId, ExamSecurityEventRequest request) {
        ensureExamExists(examId);
        String severity = request.severity() == null || request.severity().isBlank() ? "INFO" : request.severity().trim().toUpperCase();
        mapper.insertSecurityEvent(examId, request.attemptId(), userId, request.eventType().trim().toUpperCase(), severity, trim(request.detail()));
        if ("WARN".equals(severity) || "HIGH".equals(severity) || "CRITICAL".equals(severity)) {
            platformGovernanceService.notifyAll("考试安全事件", "考试 " + examId + " 收到 " + severity + " 安全事件：" + request.eventType(), "SECURITY");
        }
    }

    public List<ExamSecurityEventResponse> securityEvents(Long examId) {
        ensureExamExists(examId);
        return mapper.findSecurityEvents(examId).stream().map(this::toSecurityEvent).toList();
    }

    public List<ExamReviewRubricResponse> rubrics(Long examId) {
        ensureExamExists(examId);
        return mapper.findRubrics(examId).stream().map(this::toRubric).toList();
    }

    @Transactional
    public List<ExamReviewRubricResponse> replaceRubrics(Long examId, Long actorUserId, List<ExamReviewRubricRequest> requests) {
        ensureExamExists(examId);
        mapper.deleteRubrics(examId);
        int index = 0;
        for (ExamReviewRubricRequest request : requests) {
            mapper.insertRubric(examId, request.title().trim(), trim(request.description()), request.maxScore(), request.sortOrder() == null ? (++index * 10) : request.sortOrder());
        }
        mapper.insertEvent(examId, null, null, actorUserId, "EXAM_REVIEW_RUBRIC_UPDATED", "更新阅卷 rubric：" + requests.size() + " 条");
        return rubrics(examId);
    }

    @Transactional
    public List<ExamReviewTaskResponse> generateReviewTasks(Long examId, Long actorUserId) {
        ensureExamExists(examId);
        int created = mapper.generateReviewTasks(examId);
        mapper.insertEvent(examId, null, null, actorUserId, "EXAM_REVIEW_TASKS_GENERATED", "新增阅卷任务：" + created + " 条");
        if (created > 0) {
            platformGovernanceService.notifyAll("阅卷任务已生成", "考试 " + examId + " 新增 " + created + " 条待阅卷任务。", "REVIEW");
        }
        return reviewTasks(examId);
    }

    public List<ExamReviewTaskResponse> reviewTasks(Long examId) {
        ensureExamExists(examId);
        return mapper.findReviewTasks(examId).stream().map(this::toReviewTask).toList();
    }

    @Transactional
    public List<ExamReviewTaskResponse> claimReviewTask(Long taskId, Long actorUserId) {
        Long examId = ensureReviewTask(taskId);
        mapper.claimReviewTask(taskId, actorUserId);
        mapper.insertEvent(examId, null, actorUserId, actorUserId, "EXAM_REVIEW_TASK_CLAIMED", "领取阅卷任务 " + taskId);
        return reviewTasks(examId);
    }

    @Transactional
    public List<ExamReviewTaskResponse> updateReviewTask(Long taskId, Long actorUserId, ExamReviewTaskUpdateRequest request) {
        Long examId = ensureReviewTask(taskId);
        String status = request.status().trim().toUpperCase();
        if (!List.of("PENDING", "IN_PROGRESS", "COMPLETED").contains(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "阅卷任务状态不合法");
        }
        mapper.updateReviewTaskStatus(taskId, status);
        mapper.insertEvent(examId, null, actorUserId, actorUserId, "EXAM_REVIEW_TASK_UPDATED", "阅卷任务 " + taskId + " 更新为 " + status);
        return reviewTasks(examId);
    }

    @Transactional
    public List<ExamReviewRecheckResponse> requestRecheck(Long taskId, Long actorUserId, ExamReviewRecheckRequest request) {
        Long examId = ensureReviewTask(taskId);
        Long resultId = mapper.findReviewTaskResultId(taskId);
        mapper.insertReviewRecheck(taskId, resultId, actorUserId, trim(request.reason()));
        mapper.insertEvent(examId, null, actorUserId, actorUserId, "EXAM_REVIEW_RECHECK_REQUESTED", trim(request.reason()));
        platformGovernanceService.notifyAll("成绩复核已发起", "阅卷任务 " + taskId + " 已发起复核。", "REVIEW");
        return rechecks(examId);
    }

    @Transactional
    public List<ExamReviewRecheckResponse> updateRecheck(Long examId, Long recheckId, Long actorUserId, ExamReviewRecheckRequest request) {
        ensureExamExists(examId);
        String status = request.status() == null || request.status().isBlank() ? "RESOLVED" : request.status().trim().toUpperCase();
        if (!List.of("REQUESTED", "APPROVED", "REJECTED", "RESOLVED").contains(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "复核状态不合法");
        }
        if (mapper.updateReviewRecheck(recheckId, status, trim(request.resolution())) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "复核记录不存在");
        }
        mapper.insertEvent(examId, null, null, actorUserId, "EXAM_REVIEW_RECHECK_UPDATED", "复核 " + recheckId + " 更新为 " + status);
        return rechecks(examId);
    }

    public List<ExamReviewRecheckResponse> rechecks(Long examId) {
        ensureExamExists(examId);
        return mapper.findReviewRechecks(examId).stream().map(this::toRecheck).toList();
    }

    private void ensureExamExists(Long examId) {
        if (mapper.countExam(examId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "考试不存在");
        }
    }

    private void ensureActiveUser(Long userId) {
        if (mapper.countActiveUser(userId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在或未启用");
        }
    }

    private ExamParticipantResponse toParticipant(Map<String, Object> row) {
        return new ExamParticipantResponse(
                longValue(value(row, "userId")),
                stringValue(value(row, "username")),
                stringValue(value(row, "displayName")),
                stringValue(value(row, "departmentName")),
                stringValue(value(row, "status")),
                intValue(value(row, "extraMinutes")),
                intValue(value(row, "extraAttempts")),
                stringValue(value(row, "reason")),
                dateTimeValue(value(row, "assignedAt"))
        );
    }

    private ExamResultPolicyResponse toPolicy(Map<String, Object> row) {
        return new ExamResultPolicyResponse(
                longValue(value(row, "examId")),
                booleanValue(value(row, "visibleToStudents")),
                booleanValue(value(row, "showAnswers")),
                booleanValue(value(row, "showAnalysis")),
                dateTimeValueOrNull(value(row, "releaseTime")),
                dateTimeValue(value(row, "updatedAt"))
        );
    }

    private ExamAttemptEventResponse toEvent(Map<String, Object> row) {
        return new ExamAttemptEventResponse(
                longValue(value(row, "id")),
                longValue(value(row, "examId")),
                longValueOrNull(value(row, "attemptId")),
                longValueOrNull(value(row, "userId")),
                stringValue(value(row, "username")),
                stringValue(value(row, "actorUsername")),
                stringValue(value(row, "action")),
                stringValue(value(row, "reason")),
                dateTimeValue(value(row, "createdAt"))
        );
    }

    private ExamSecurityPolicyResponse toSecurityPolicy(Map<String, Object> row) {
        return new ExamSecurityPolicyResponse(
                longValue(value(row, "examId")),
                booleanValue(value(row, "requireFullscreen")),
                booleanValue(value(row, "forbidCopyPaste")),
                booleanValue(value(row, "trackFocusLoss")),
                intValue(value(row, "maxFocusLossCount")),
                booleanValue(value(row, "deviceCheckRequired")),
                dateTimeValue(value(row, "updatedAt"))
        );
    }

    private ExamSecurityEventResponse toSecurityEvent(Map<String, Object> row) {
        return new ExamSecurityEventResponse(
                longValue(value(row, "id")),
                longValue(value(row, "examId")),
                longValueOrNull(value(row, "attemptId")),
                longValue(value(row, "userId")),
                stringValue(value(row, "username")),
                stringValue(value(row, "eventType")),
                stringValue(value(row, "severity")),
                stringValue(value(row, "detail")),
                dateTimeValue(value(row, "occurredAt"))
        );
    }

    private ExamReviewRubricResponse toRubric(Map<String, Object> row) {
        return new ExamReviewRubricResponse(
                longValue(value(row, "id")),
                longValue(value(row, "examId")),
                stringValue(value(row, "title")),
                stringValue(value(row, "description")),
                decimalValue(value(row, "maxScore")),
                intValue(value(row, "sortOrder"))
        );
    }

    private ExamReviewTaskResponse toReviewTask(Map<String, Object> row) {
        return new ExamReviewTaskResponse(
                longValue(value(row, "id")),
                longValue(value(row, "resultId")),
                longValue(value(row, "examId")),
                stringValue(value(row, "examTitle")),
                longValueOrNull(value(row, "reviewerId")),
                stringValue(value(row, "reviewerUsername")),
                stringValue(value(row, "status")),
                stringValue(value(row, "studentName")),
                dateTimeValueOrNull(value(row, "assignedAt")),
                dateTimeValueOrNull(value(row, "completedAt")),
                dateTimeValue(value(row, "createdAt"))
        );
    }

    private ExamReviewRecheckResponse toRecheck(Map<String, Object> row) {
        return new ExamReviewRecheckResponse(
                longValue(value(row, "id")),
                longValue(value(row, "taskId")),
                longValue(value(row, "resultId")),
                stringValue(value(row, "requestedBy")),
                stringValue(value(row, "status")),
                stringValue(value(row, "reason")),
                stringValue(value(row, "resolution")),
                dateTimeValue(value(row, "createdAt")),
                dateTimeValueOrNull(value(row, "resolvedAt"))
        );
    }

    private Long ensureReviewTask(Long taskId) {
        Long examId = mapper.findReviewTaskExamId(taskId);
        if (examId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "阅卷任务不存在");
        }
        return examId;
    }

    private Object valueOrNull(Map<String, Object> row, String key) {
        return row == null ? null : value(row, key);
    }

    private Integer intValueOrZero(Object value) {
        return value == null ? 0 : intValue(value);
    }

    private Long longValueOrNull(Object value) {
        return value == null ? null : longValue(value);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
