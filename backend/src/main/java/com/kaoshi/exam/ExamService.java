package com.kaoshi.exam;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.common.page.PageRequest;
import com.kaoshi.common.page.PageResponse;
import com.kaoshi.exam.domain.Exam;
import com.kaoshi.exam.dto.AnswerSubmitItem;
import com.kaoshi.exam.dto.ExamQuestionOptionResponse;
import com.kaoshi.exam.dto.ExamQuestionResponse;
import com.kaoshi.exam.dto.ExamResponse;
import com.kaoshi.exam.dto.ExamResultResponse;
import com.kaoshi.exam.dto.ExamSaveRequest;
import com.kaoshi.exam.dto.ExamSessionResponse;
import com.kaoshi.exam.dto.ExamSubmitRequest;
import com.kaoshi.exam.mapper.ExamMapper;
import com.kaoshi.question.dto.QuestionAttachmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExamService {
    private final ExamMapper examMapper;

    public ExamService(ExamMapper examMapper) {
        this.examMapper = examMapper;
    }

    public PageResponse<ExamResponse> page(PageRequest request) {
        long total = examMapper.countExams(request.keywordLike());
        List<ExamResponse> records = examMapper.findExams(request.keywordLike(), request.size(), request.offset())
                .stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(records, total, request.page(), request.size());
    }

    public ExamResponse detail(Long id) {
        return toResponse(findExam(id));
    }

    @Transactional
    public ExamResponse create(ExamSaveRequest request) {
        validateExam(request);
        Exam exam = new Exam();
        fillExam(exam, request);
        examMapper.insertExam(exam);
        return detail(exam.getId());
    }

    @Transactional
    public ExamResponse update(Long id, ExamSaveRequest request) {
        validateExam(request);
        Exam exam = findExam(id);
        fillExam(exam, request);
        examMapper.updateExam(exam);
        return detail(id);
    }

    public List<ExamResponse> publishedExams() {
        return examMapper.findPublishedExams().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExamSessionResponse startExam(Long examId, Long userId) {
        Exam exam = findExam(examId);
        ensureExamAvailable(exam);
        Map<String, Object> attempt = examMapper.findAttempt(examId, userId);
        if (attempt == null) {
            attempt = new HashMap<>();
            attempt.put("examId", examId);
            attempt.put("userId", userId);
            examMapper.insertAttempt(attempt);
            attempt = examMapper.findAttempt(examId, userId);
        }
        return sessionResponse(exam, attempt);
    }

    @Transactional
    public ExamResultResponse submit(Long examId, Long userId, ExamSubmitRequest request) {
        Exam exam = findExam(examId);
        ensureExamAvailable(exam);
        Map<String, Object> attempt = examMapper.findAttempt(examId, userId);
        if (attempt == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "考试尚未开始");
        }
        if (!"IN_PROGRESS".equals(stringValue(value(attempt, "status")))) {
            throw new BusinessException(ErrorCode.CONFLICT, "考试已提交，不能重复提交");
        }

        Map<Long, AnswerSubmitItem> submitted = new HashMap<>();
        for (AnswerSubmitItem answer : request.answers()) {
            submitted.put(answer.questionId(), answer);
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal obtainedScore = BigDecimal.ZERO;
        int correctCount = 0;
        List<Map<String, Object>> questions = examMapper.findExamQuestions(examId);
        for (Map<String, Object> question : questions) {
            Long questionId = longValue(value(question, "questionId"));
            BigDecimal questionScore = decimalValue(value(question, "score"));
            totalScore = totalScore.add(questionScore);
            AnswerSubmitItem answer = submitted.get(questionId);
            List<String> selected = answer == null ? List.of() : normalizedLabels(answer.selectedLabels());
            List<String> correct = normalizedLabels(examMapper.findCorrectLabels(questionId));
            boolean right = selected.equals(correct);
            BigDecimal score = right ? questionScore : BigDecimal.ZERO;
            if (right) {
                correctCount++;
                obtainedScore = obtainedScore.add(score);
            }
            examMapper.insertAnswer(longValue(value(attempt, "id")), questionId, String.join(",", selected), right, score);
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        int durationSeconds = Math.max(0, (int) Duration.between(dateTimeValue(value(attempt, "startedAt")), submittedAt).toSeconds());
        int updated = examMapper.submitAttempt(longValue(value(attempt, "id")), submittedAt, totalScore, obtainedScore, durationSeconds);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "考试已提交，不能重复提交");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", longValue(value(attempt, "id")));
        result.put("examId", examId);
        result.put("userId", userId);
        result.put("totalScore", totalScore);
        result.put("obtainedScore", obtainedScore);
        result.put("correctCount", correctCount);
        result.put("questionCount", questions.size());
        result.put("submittedAt", submittedAt);
        examMapper.insertResult(result);
        return new ExamResultResponse(
                longValue(value(result, "id")),
                longValue(value(result, "attemptId")),
                examId,
                exam.getTitle(),
                userId,
                totalScore,
                obtainedScore,
                correctCount,
                questions.size(),
                submittedAt
        );
    }

    public List<ExamResultResponse> adminResults() {
        return examMapper.findResults().stream().map(this::toResultResponse).toList();
    }

    public List<ExamResultResponse> userResults(Long userId) {
        return examMapper.findResultsByUser(userId).stream().map(this::toResultResponse).toList();
    }

    private Exam findExam(Long id) {
        Exam exam = examMapper.findExamById(id);
        if (exam == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "考试不存在");
        }
        return exam;
    }

    private void validateExam(ExamSaveRequest request) {
        if (examMapper.countActivePaperById(request.paperId()) == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试卷不存在或未启用");
        }
        if (!List.of("DRAFT", "PUBLISHED", "CLOSED").contains(request.status())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "考试状态不合法");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "考试结束时间必须晚于开始时间");
        }
    }

    private void fillExam(Exam exam, ExamSaveRequest request) {
        exam.setPaperId(request.paperId());
        exam.setTitle(request.title());
        exam.setDescription(request.description());
        exam.setStartTime(request.startTime());
        exam.setEndTime(request.endTime());
        exam.setDurationMinutes(request.durationMinutes());
        exam.setStatus(request.status());
    }

    private void ensureExamAvailable(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (!"PUBLISHED".equals(exam.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "考试未发布");
        }
        if (now.isBefore(exam.getStartTime()) || now.isAfter(exam.getEndTime())) {
            throw new BusinessException(ErrorCode.CONFLICT, "不在考试时间内");
        }
    }

    private ExamSessionResponse sessionResponse(Exam exam, Map<String, Object> attempt) {
        return new ExamSessionResponse(
                exam.getId(),
                longValue(value(attempt, "id")),
                exam.getTitle(),
                exam.getDurationMinutes(),
                dateTimeValue(value(attempt, "startedAt")),
                stringValue(value(attempt, "status")),
                examMapper.findExamQuestions(exam.getId()).stream()
                        .map(this::toExamQuestionResponse)
                        .toList()
        );
    }

    private ExamQuestionResponse toExamQuestionResponse(Map<String, Object> row) {
        Long questionId = longValue(value(row, "questionId"));
        return new ExamQuestionResponse(
                questionId,
                stringValue(value(row, "type")),
                stringValue(value(row, "stem")),
                decimalValue(value(row, "score")),
                intValue(value(row, "sortOrder")),
                examMapper.findQuestionAttachments(questionId).stream().map(this::toAttachmentResponse).toList(),
                examMapper.findQuestionOptions(questionId).stream().map(this::toOptionResponse).toList()
        );
    }

    private QuestionAttachmentResponse toAttachmentResponse(Map<String, Object> row) {
        return new QuestionAttachmentResponse(
                longValue(value(row, "id")),
                stringValue(value(row, "fileName")),
                stringValue(value(row, "fileUrl")),
                stringValue(value(row, "mediaType")),
                intValue(value(row, "sortOrder"))
        );
    }

    private ExamQuestionOptionResponse toOptionResponse(Map<String, Object> row) {
        return new ExamQuestionOptionResponse(
                longValue(value(row, "id")),
                stringValue(value(row, "label")),
                stringValue(value(row, "content")),
                intValue(value(row, "sortOrder"))
        );
    }

    private ExamResponse toResponse(Exam exam) {
        return new ExamResponse(
                exam.getId(),
                exam.getPaperId(),
                examMapper.findPaperName(exam.getPaperId()),
                exam.getTitle(),
                exam.getDescription(),
                exam.getStartTime(),
                exam.getEndTime(),
                exam.getDurationMinutes(),
                exam.getStatus()
        );
    }

    private ExamResultResponse toResultResponse(Map<String, Object> row) {
        return new ExamResultResponse(
                longValue(value(row, "id")),
                longValue(value(row, "attemptId")),
                longValue(value(row, "examId")),
                stringValue(value(row, "examTitle")),
                longValue(value(row, "userId")),
                decimalValue(value(row, "totalScore")),
                decimalValue(value(row, "obtainedScore")),
                intValue(value(row, "correctCount")),
                intValue(value(row, "questionCount")),
                dateTimeValue(value(row, "submittedAt"))
        );
    }

    private List<String> normalizedLabels(List<String> labels) {
        return labels.stream()
                .map(String::trim)
                .filter(label -> !label.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .toList();
    }

    private Long longValue(Object value) {
        return ((Number) value).longValue();
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String upperSnake = key.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        if (row.containsKey(upperSnake)) {
            return row.get(upperSnake);
        }
        String lowerSnake = upperSnake.toLowerCase();
        if (row.containsKey(lowerSnake)) {
            return row.get(lowerSnake);
        }
        String upper = key.toUpperCase();
        if (row.containsKey(upper)) {
            return row.get(upper);
        }
        return row.get(key.toLowerCase());
    }

    private Integer intValue(Object value) {
        return ((Number) value).intValue();
    }

    private BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime dateTimeValue(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString());
    }
}

