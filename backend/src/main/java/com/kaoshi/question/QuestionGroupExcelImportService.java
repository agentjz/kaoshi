package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.common.excel.ExcelImportResult;
import com.kaoshi.common.excel.ExcelWorkbooks;
import com.kaoshi.question.domain.QuestionNode;
import com.kaoshi.question.dto.QuestionOptionRequest;
import com.kaoshi.question.dto.QuestionSaveRequest;
import com.kaoshi.question.mapper.QuestionContentMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuestionGroupExcelImportService {
    private final QuestionContentMapper contentMapper;
    private final QuestionService questionService;

    public QuestionGroupExcelImportService(QuestionContentMapper contentMapper, QuestionService questionService) {
        this.contentMapper = contentMapper;
        this.questionService = questionService;
    }

    @Transactional
    public ExcelImportResult importToGroup(Long nodeId, MultipartFile file) {
        QuestionNode node = contentMapper.findNodeById(nodeId);
        if (node == null || !"GROUP".equals(node.getNodeType())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "题组不存在");
        }
        Long bankId = node.getBankId();
        Set<String> sharedLabels = sharedLabels(nodeId);
        List<String> errors = new ArrayList<>();
        int success = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelWorkbooks.text(row, 3).isBlank()) {
                    continue;
                }
                try {
                    questionService.create(rowToRequest(bankId, nodeId, row, sharedLabels));
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

    private QuestionSaveRequest rowToRequest(Long bankId, Long nodeId, Row row, Set<String> sharedLabels) {
        String itemLabel = ExcelWorkbooks.text(row, 0).trim();
        String type = normalizeType(ExcelWorkbooks.text(row, 1).trim());
        String difficulty = normalizeDifficulty(ExcelWorkbooks.text(row, 2).trim());
        String stem = ExcelWorkbooks.text(row, 3).trim();
        if (stem.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题干不能为空");
        }
        boolean optionBased = QuestionType.require(type).optionBased();
        List<String> correctLabels = optionBased ? parseCorrectLabels(ExcelWorkbooks.text(row, 8)) : List.of();
        List<QuestionOptionRequest> options = rowOptions(row, correctLabels);
        if (options.isEmpty() && sharedLabels.isEmpty() && optionBased) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题组没有共享选项时，Excel 必须填写选项");
        }
        for (String label : correctLabels) {
            if (options.isEmpty() && !sharedLabels.contains(label)) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "正确答案引用了不存在的共享选项：" + label);
            }
        }
        return new QuestionSaveRequest(
                bankId,
                nodeId,
                type,
                stem,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                itemLabel.isBlank() ? null : itemLabel,
                stem,
                ExcelWorkbooks.text(row, 9).trim(),
                difficulty,
                normalizeStatus(ExcelWorkbooks.text(row, 10).trim()),
                options,
                correctLabels,
                List.of()
        );
    }

    private List<QuestionOptionRequest> rowOptions(Row row, List<String> correctLabels) {
        List<QuestionOptionRequest> options = new ArrayList<>();
        for (int index = 0; index < 4; index++) {
            String label = String.valueOf((char) ('A' + index));
            String content = ExcelWorkbooks.text(row, 4 + index).trim();
            if (!content.isBlank()) {
                options.add(new QuestionOptionRequest(label, content, correctLabels.contains(label)));
            }
        }
        return options;
    }

    private Set<String> sharedLabels(Long nodeId) {
        return contentMapper.findNodeOptions(nodeId).stream()
                .map(option -> option.getOptionLabel())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> parseCorrectLabels(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (normalized.contains(",") || normalized.contains("，")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "正确答案请使用 AC，不要使用 A,C");
        }
        List<String> labels = normalized.chars()
                .mapToObj(character -> String.valueOf((char) character))
                .filter(label -> !label.isBlank())
                .distinct()
                .toList();
        if (labels.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "正确答案不能为空");
        }
        return labels;
    }

    private String normalizeType(String value) {
        if ("单选".equals(value) || "单选题".equals(value) || QuestionType.SINGLE_CHOICE.code().equals(value)) {
            return QuestionType.SINGLE_CHOICE.code();
        }
        if ("多选".equals(value) || "多选题".equals(value) || QuestionType.MULTIPLE_CHOICE.code().equals(value)) {
            return QuestionType.MULTIPLE_CHOICE.code();
        }
        if ("选词填空".equals(value) || "选词填空题".equals(value) || QuestionType.WORD_BANK.code().equals(value)) {
            return QuestionType.WORD_BANK.code();
        }
        if ("匹配".equals(value) || "匹配题".equals(value) || QuestionType.MATCHING.code().equals(value)) {
            return QuestionType.MATCHING.code();
        }
        if ("写作".equals(value) || "写作题".equals(value) || QuestionType.WRITING.code().equals(value)) {
            return QuestionType.WRITING.code();
        }
        if ("翻译".equals(value) || "翻译题".equals(value) || QuestionType.TRANSLATION.code().equals(value)) {
            return QuestionType.TRANSLATION.code();
        }
        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试题类型不支持：" + value);
    }

    private String normalizeDifficulty(String value) {
        if (value.isBlank() || "简单".equals(value) || "EASY".equals(value)) {
            return "EASY";
        }
        if ("困难".equals(value) || "HARD".equals(value)) {
            return "HARD";
        }
        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试题难度不合法：" + value);
    }

    private String normalizeStatus(String value) {
        if (value.isBlank() || "启用".equals(value) || "ACTIVE".equals(value)) {
            return "ACTIVE";
        }
        if ("禁用".equals(value) || "DISABLED".equals(value)) {
            return "DISABLED";
        }
        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "试题状态不合法：" + value);
    }

}
