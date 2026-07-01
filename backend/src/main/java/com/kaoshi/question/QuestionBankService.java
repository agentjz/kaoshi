package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.common.page.PageRequest;
import com.kaoshi.common.page.PageResponse;
import com.kaoshi.question.domain.QuestionBank;
import com.kaoshi.question.dto.QuestionBankResponse;
import com.kaoshi.question.dto.QuestionBankSaveRequest;
import com.kaoshi.question.mapper.QuestionBankMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionBankService {
    private final QuestionBankMapper bankMapper;

    public QuestionBankService(QuestionBankMapper bankMapper) {
        this.bankMapper = bankMapper;
    }

    public PageResponse<QuestionBankResponse> page(PageRequest request) {
        long total = bankMapper.countBanks(request.keywordLike());
        List<QuestionBankResponse> records = bankMapper.findBanks(request.keywordLike(), request.size(), request.offset())
                .stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(records, total, request.page(), request.size());
    }

    public List<?> categories() {
        return bankMapper.findCategories();
    }

    public QuestionBankResponse detail(Long id) {
        return toResponse(findBank(id));
    }

    @Transactional
    public QuestionBankResponse create(QuestionBankSaveRequest request) {
        ensureCategoryExists(request.categoryId());
        QuestionBank bank = new QuestionBank();
        fillBank(bank, request);
        bankMapper.insertBank(bank);
        return detail(bank.getId());
    }

    @Transactional
    public QuestionBankResponse update(Long id, QuestionBankSaveRequest request) {
        ensureCategoryExists(request.categoryId());
        QuestionBank bank = findBank(id);
        fillBank(bank, request);
        bankMapper.updateBank(bank);
        return detail(id);
    }

    private QuestionBank findBank(Long id) {
        QuestionBank bank = bankMapper.findBankById(id);
        if (bank == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "题库不存在");
        }
        return bank;
    }

    private void fillBank(QuestionBank bank, QuestionBankSaveRequest request) {
        if (!List.of("ACTIVE", "DISABLED").contains(request.status())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题库状态不合法");
        }
        bank.setCategoryId(request.categoryId());
        bank.setName(request.name());
        bank.setDescription(request.description());
        bank.setStatus(request.status());
    }

    private void ensureCategoryExists(Long categoryId) {
        if (bankMapper.countCategoryById(categoryId) == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试题分类不存在");
        }
    }

    private QuestionBankResponse toResponse(QuestionBank bank) {
        return new QuestionBankResponse(
                bank.getId(),
                bank.getCategoryId(),
                bankMapper.findCategoryName(bank.getCategoryId()),
                bank.getName(),
                bank.getDescription(),
                bank.getStatus()
        );
    }
}

