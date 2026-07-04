package com.kaoshi.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.question.domain.QuestionBank;
import com.kaoshi.question.domain.QuestionCategory;
import com.kaoshi.question.dto.QuestionBankPackageImportResponse;
import com.kaoshi.question.dto.QuestionContentNodeResponse;
import com.kaoshi.question.dto.QuestionContentTreeResponse;
import com.kaoshi.question.dto.QuestionNodeSaveRequest;
import com.kaoshi.question.dto.QuestionSaveRequest;
import com.kaoshi.question.mapper.QuestionBankMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class QuestionBankPackageService {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final QuestionContentService contentService;
    private final QuestionBankMapper bankMapper;
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;
    private final QuestionBankPackageZipCodec zipCodec;
    private final QuestionBankPackageWorkbookCodec workbookCodec;
    private final QuestionBankPackageAssetStore assetStore;

    public QuestionBankPackageService(
            QuestionContentService contentService,
            QuestionBankMapper bankMapper,
            QuestionService questionService,
            ObjectMapper objectMapper,
            QuestionBankPackageZipCodec zipCodec,
            QuestionBankPackageWorkbookCodec workbookCodec,
            QuestionBankPackageAssetStore assetStore
    ) {
        this.contentService = contentService;
        this.bankMapper = bankMapper;
        this.questionService = questionService;
        this.objectMapper = objectMapper;
        this.zipCodec = zipCodec;
        this.workbookCodec = workbookCodec;
        this.assetStore = assetStore;
    }

    public ResponseEntity<byte[]> exportPackage(Long bankId) {
        QuestionContentTreeResponse tree = contentService.tree(bankId);
        byte[] body = zipCodec.write(
                manifest(bankMapper.findCategoryNameByBankId(bankId), tree),
                workbookCodec.write(tree),
                assetStore.exportAssets(tree)
        );
        String encoded = URLEncoder.encode(safeFilename(tree.bankName()) + "-题库包.zip", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(encoded, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    @Transactional
    public QuestionBankPackageImportResponse importPackage(MultipartFile file) {
        QuestionBankPackageEntries entries = zipCodec.read(file);
        QuestionBankPackageManifest manifest = readManifest(entries);
        Long categoryId = ensureCategory(manifest.categoryName());
        QuestionBank bank = createBank(categoryId, uniqueBankName(manifest.bankName()));
        QuestionBankPackageWorkbook workbook = workbookCodec.read(entries);
        ImportIndexes indexes = createNodes(bank.getId(), workbook);
        int questionCount = createQuestions(bank.getId(), indexes, workbook);
        return new QuestionBankPackageImportResponse(bank.getId(), bank.getName(), indexes.nodeCount(), questionCount);
    }

    private byte[] manifest(String categoryName, QuestionContentTreeResponse tree) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(Map.of(
                    "version", "1",
                    "exportedAt", LocalDateTime.now().toString(),
                    "categoryName", categoryName,
                    "bankName", tree.bankName(),
                    "sectionCount", tree.sections().size(),
                    "ungroupedQuestionCount", tree.ungroupedQuestions().size()
            ));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "生成题库包 manifest 失败");
        }
    }

    private QuestionBankPackageManifest readManifest(QuestionBankPackageEntries entries) {
        try {
            return objectMapper.readValue(entries.required("manifest.json"), QuestionBankPackageManifest.class);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题库包 manifest 不合法");
        }
    }

    private Long ensureCategory(String categoryName) {
        QuestionCategory existing = bankMapper.findCategoryByName(categoryName);
        if (existing != null) {
            return existing.getId();
        }
        QuestionCategory category = new QuestionCategory();
        category.setName(categoryName);
        category.setDescription("题库包导入创建");
        category.setSortOrder(0);
        bankMapper.insertCategory(category);
        return category.getId();
    }

    private QuestionBank createBank(Long categoryId, String name) {
        QuestionBank bank = new QuestionBank();
        bank.setCategoryId(categoryId);
        bank.setName(name);
        bank.setDescription("题库包导入创建");
        bank.setStatus("ACTIVE");
        bankMapper.insertBank(bank);
        return bank;
    }

    private ImportIndexes createNodes(Long bankId, QuestionBankPackageWorkbook workbook) {
        Map<String, Long> sectionIds = new HashMap<>();
        Map<String, Long> groupIds = new HashMap<>();
        for (QuestionBankPackageWorkbook.SectionRow section : workbook.sections()) {
            QuestionContentNodeResponse saved = contentService.createNode(bankId, new QuestionNodeSaveRequest(
                    null, section.code(), "SECTION", section.title(), section.direction(), section.material(),
                    section.sortOrder(), workbook.nodeOptions(section.code()), workbook.nodeAttachments(section.code())
            ));
            sectionIds.put(section.code(), saved.id());
        }
        for (QuestionBankPackageWorkbook.GroupRow group : workbook.groups()) {
            Long parentId = group.sectionCode().isBlank() ? null : sectionIds.get(group.sectionCode());
            if (parentId == null && !group.sectionCode().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题组引用的大组不存在：" + group.sectionCode());
            }
            QuestionContentNodeResponse saved = contentService.createNode(bankId, new QuestionNodeSaveRequest(
                    parentId, group.code(), "GROUP", group.title(), group.direction(), group.material(),
                    group.sortOrder(), workbook.nodeOptions(group.code()), workbook.nodeAttachments(group.code())
            ));
            groupIds.put(group.code(), saved.id());
        }
        return new ImportIndexes(sectionIds, groupIds);
    }

    private int createQuestions(Long bankId, ImportIndexes indexes, QuestionBankPackageWorkbook workbook) {
        int questionCount = 0;
        for (QuestionBankPackageWorkbook.ItemRow item : workbook.items()) {
            Long nodeId = item.groupCode().isBlank() ? null : indexes.groupIds().get(item.groupCode());
            if (nodeId == null && !item.groupCode().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "小题引用的题组不存在：" + item.groupCode());
            }
            questionService.create(new QuestionSaveRequest(
                    bankId, nodeId, item.type(), item.stem(), null, null, null, null, null, null, null, null,
                    item.label(), item.itemStem(), item.analysis(), item.difficulty(), item.status(),
                    workbook.itemOptions(item.code(), item.answerLabels()), item.answerLabels(), workbook.itemAttachments(item.code())
            ));
            questionCount++;
        }
        return questionCount;
    }

    private String uniqueBankName(String bankName) {
        if (bankMapper.countBankByName(bankName) == 0) {
            return bankName;
        }
        return bankName + "-导入-" + LocalDateTime.now().format(STAMP);
    }

    private String safeFilename(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private record ImportIndexes(Map<String, Long> sectionIds, Map<String, Long> groupIds) {
        int nodeCount() {
            return sectionIds.size() + groupIds.size();
        }
    }
}
