package com.kaoshi.paper;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.common.page.PageRequest;
import com.kaoshi.common.page.PageResponse;
import com.kaoshi.paper.domain.Paper;
import com.kaoshi.paper.domain.PaperQuestion;
import com.kaoshi.paper.dto.PaperQuestionRequest;
import com.kaoshi.paper.dto.PaperQuestionResponse;
import com.kaoshi.paper.dto.PaperResponse;
import com.kaoshi.paper.dto.PaperSaveRequest;
import com.kaoshi.paper.mapper.PaperMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PaperService {
    private final PaperMapper paperMapper;

    public PaperService(PaperMapper paperMapper) {
        this.paperMapper = paperMapper;
    }

    public List<?> categories() {
        return paperMapper.findCategories();
    }

    public PageResponse<PaperResponse> page(PageRequest request) {
        long total = paperMapper.countPapers(request.keywordLike());
        List<PaperResponse> records = paperMapper.findPapers(request.keywordLike(), request.size(), request.offset())
                .stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(records, total, request.page(), request.size());
    }

    public PaperResponse detail(Long id) {
        return toResponse(findPaper(id));
    }

    @Transactional
    public PaperResponse create(PaperSaveRequest request) {
        validatePaper(request);
        Paper paper = new Paper();
        fillPaper(paper, request);
        paperMapper.insertPaper(paper);
        replaceQuestions(paper.getId(), request.questions());
        return detail(paper.getId());
    }

    @Transactional
    public PaperResponse update(Long id, PaperSaveRequest request) {
        validatePaper(request);
        Paper paper = findPaper(id);
        fillPaper(paper, request);
        paperMapper.updatePaper(paper);
        replaceQuestions(id, request.questions());
        return detail(id);
    }

    private Paper findPaper(Long id) {
        Paper paper = paperMapper.findPaperById(id);
        if (paper == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "试卷不存在");
        }
        return paper;
    }

    private void validatePaper(PaperSaveRequest request) {
        if (paperMapper.countCategoryById(request.categoryId()) == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试卷分类不存在");
        }
        if (!List.of("ACTIVE", "DISABLED").contains(request.status())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试卷状态不合法");
        }
        Set<Long> questionIds = new HashSet<>();
        for (PaperQuestionRequest question : request.questions()) {
            if (!questionIds.add(question.questionId())) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试卷题目不能重复");
            }
            if (paperMapper.countActiveQuestionById(question.questionId()) == 0) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试题不存在或未启用");
            }
        }
    }

    private void fillPaper(Paper paper, PaperSaveRequest request) {
        paper.setCategoryId(request.categoryId());
        paper.setName(request.name());
        paper.setDescription(request.description());
        paper.setDurationMinutes(request.durationMinutes());
        paper.setStatus(request.status());
        paper.setTotalScore(request.questions().stream()
                .map(PaperQuestionRequest::score)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void replaceQuestions(Long paperId, List<PaperQuestionRequest> questions) {
        paperMapper.deletePaperQuestions(paperId);
        int sort = 10;
        for (PaperQuestionRequest request : questions) {
            PaperQuestion paperQuestion = new PaperQuestion();
            paperQuestion.setPaperId(paperId);
            paperQuestion.setQuestionId(request.questionId());
            paperQuestion.setScore(request.score());
            paperQuestion.setSortOrder(sort);
            paperMapper.insertPaperQuestion(paperQuestion);
            sort += 10;
        }
    }

    private PaperResponse toResponse(Paper paper) {
        return new PaperResponse(
                paper.getId(),
                paper.getCategoryId(),
                paperMapper.findCategoryName(paper.getCategoryId()),
                paper.getName(),
                paper.getDescription(),
                paper.getTotalScore(),
                paper.getDurationMinutes(),
                paper.getStatus(),
                paperMapper.findPaperQuestions(paper.getId()).stream()
                        .map(this::toQuestionResponse)
                        .toList()
        );
    }

    private PaperQuestionResponse toQuestionResponse(PaperQuestion paperQuestion) {
        return new PaperQuestionResponse(
                paperQuestion.getId(),
                paperQuestion.getQuestionId(),
                paperMapper.findQuestionType(paperQuestion.getQuestionId()),
                paperMapper.findQuestionStem(paperQuestion.getQuestionId()),
                paperQuestion.getScore(),
                paperQuestion.getSortOrder()
        );
    }
}

