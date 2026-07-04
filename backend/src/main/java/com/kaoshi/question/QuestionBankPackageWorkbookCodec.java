package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.common.excel.ExcelWorkbooks;
import com.kaoshi.question.dto.QuestionAttachmentRequest;
import com.kaoshi.question.dto.QuestionAttachmentResponse;
import com.kaoshi.question.dto.QuestionContentNodeResponse;
import com.kaoshi.question.dto.QuestionContentTreeResponse;
import com.kaoshi.question.dto.QuestionNodeOptionRequest;
import com.kaoshi.question.dto.QuestionResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
class QuestionBankPackageWorkbookCodec {
    private final QuestionBankPackageAssetStore assetStore;

    QuestionBankPackageWorkbookCodec(QuestionBankPackageAssetStore assetStore) {
        this.assetStore = assetStore;
    }

    byte[] write(QuestionContentTreeResponse tree) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writeSheet(workbook, "sections", List.of("section_code", "title", "direction", "material", "sort_order"), sectionRows(tree));
            writeSheet(workbook, "groups", List.of("group_code", "section_code", "title", "direction", "material", "sort_order"), groupRows(tree));
            writeSheet(workbook, "group_options", List.of("node_code", "label", "content"), nodeOptionRows(tree));
            writeSheet(workbook, "items", List.of("item_code", "group_code", "item_label", "type", "stem", "item_stem", "answer", "analysis", "difficulty", "status"), itemRows(tree));
            writeSheet(workbook, "item_options", List.of("item_code", "label", "content", "correct"), itemOptionRows(tree));
            writeSheet(workbook, "attachments", List.of("target_type", "target_code", "file_name", "file_url", "media_type"), attachmentRows(tree));
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "生成题库包 Excel 失败");
        }
    }

    QuestionBankPackageWorkbook read(QuestionBankPackageEntries entries) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(entries.required("content.xlsx")))) {
            List<QuestionBankPackageWorkbook.SectionRow> sections = rows(workbook, "sections").stream()
                    .map(row -> new QuestionBankPackageWorkbook.SectionRow(cell(row, 0), cell(row, 1), cell(row, 2), cell(row, 3), number(cell(row, 4))))
                    .toList();
            List<QuestionBankPackageWorkbook.GroupRow> groups = rows(workbook, "groups").stream()
                    .map(row -> new QuestionBankPackageWorkbook.GroupRow(cell(row, 0), cell(row, 1), cell(row, 2), cell(row, 3), cell(row, 4), number(cell(row, 5))))
                    .toList();
            List<QuestionBankPackageWorkbook.ItemRow> items = rows(workbook, "items").stream()
                    .map(row -> new QuestionBankPackageWorkbook.ItemRow(cell(row, 0), cell(row, 1), cell(row, 2), cell(row, 3), cell(row, 4), cell(row, 5), labels(cell(row, 6)), cell(row, 7), blankDefault(cell(row, 8), "EASY"), blankDefault(cell(row, 9), "ACTIVE")))
                    .toList();
            return new QuestionBankPackageWorkbook(
                    sections,
                    groups,
                    items,
                    nodeOptions(workbook),
                    itemOptions(workbook),
                    attachments(workbook, entries, "NODE"),
                    attachments(workbook, entries, "ITEM")
            );
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "读取题库包 Excel 失败");
        }
    }

    private List<List<String>> sectionRows(QuestionContentTreeResponse tree) {
        return tree.sections().stream()
                .map(section -> List.of(text(section.nodeCode()), text(section.title()), text(section.direction()), text(section.material()), text(section.sortOrder())))
                .toList();
    }

    private List<List<String>> groupRows(QuestionContentTreeResponse tree) {
        List<List<String>> rows = new ArrayList<>();
        for (QuestionContentNodeResponse section : tree.sections()) {
            for (QuestionContentNodeResponse group : section.children()) {
                rows.add(List.of(text(group.nodeCode()), text(section.nodeCode()), text(group.title()), text(group.direction()), text(group.material()), text(group.sortOrder())));
            }
        }
        return rows;
    }

    private List<List<String>> nodeOptionRows(QuestionContentTreeResponse tree) {
        List<List<String>> rows = new ArrayList<>();
        forEachNode(tree, node -> node.sharedOptions().forEach(option ->
                rows.add(List.of(text(node.nodeCode()), text(option.label()), text(option.content())))));
        return rows;
    }

    private List<List<String>> itemRows(QuestionContentTreeResponse tree) {
        List<List<String>> rows = new ArrayList<>();
        forEachQuestion(tree, (groupCode, question) -> rows.add(List.of(
                itemCode(question), text(groupCode), text(question.itemLabel()), text(question.type()),
                text(question.stem()), text(question.itemStem()), answer(question), text(question.analysis()),
                text(question.difficulty()), text(question.status())
        )));
        return rows;
    }

    private List<List<String>> itemOptionRows(QuestionContentTreeResponse tree) {
        List<List<String>> rows = new ArrayList<>();
        forEachQuestion(tree, (groupCode, question) -> {
            if (hasSharedOptions(tree, groupCode)) {
                return;
            }
            question.options().forEach(option -> rows.add(List.of(itemCode(question), text(option.label()), text(option.content()), option.correct() ? "TRUE" : "FALSE")));
        });
        return rows;
    }

    private List<List<String>> attachmentRows(QuestionContentTreeResponse tree) {
        List<List<String>> rows = new ArrayList<>();
        forEachNode(tree, node -> node.attachments().forEach(attachment -> rows.add(attachmentRow("NODE", node.nodeCode(), attachment))));
        forEachQuestion(tree, (groupCode, question) -> question.attachments().forEach(attachment -> rows.add(attachmentRow("ITEM", itemCode(question), attachment))));
        return rows;
    }

    private List<String> attachmentRow(String targetType, String targetCode, QuestionAttachmentResponse attachment) {
        return List.of(targetType, text(targetCode), text(attachment.fileName()), assetStore.packageFileUrl(attachment.fileUrl()), text(attachment.mediaType()));
    }

    private Map<String, List<QuestionNodeOptionRequest>> nodeOptions(Workbook workbook) {
        Map<String, List<QuestionNodeOptionRequest>> nodeOptions = new HashMap<>();
        for (Row row : rows(workbook, "group_options")) {
            nodeOptions.computeIfAbsent(cell(row, 0), key -> new ArrayList<>())
                    .add(new QuestionNodeOptionRequest(cell(row, 1), cell(row, 2)));
        }
        return nodeOptions;
    }

    private Map<String, List<QuestionBankPackageWorkbook.QuestionOptionSeed>> itemOptions(Workbook workbook) {
        Map<String, List<QuestionBankPackageWorkbook.QuestionOptionSeed>> itemOptions = new HashMap<>();
        for (Row row : rows(workbook, "item_options")) {
            itemOptions.computeIfAbsent(cell(row, 0), key -> new ArrayList<>())
                    .add(new QuestionBankPackageWorkbook.QuestionOptionSeed(cell(row, 1), cell(row, 2), truthy(cell(row, 3))));
        }
        return itemOptions;
    }

    private Map<String, List<QuestionAttachmentRequest>> attachments(Workbook workbook, QuestionBankPackageEntries entries, String targetType) {
        Map<String, List<QuestionAttachmentRequest>> attachments = new HashMap<>();
        for (Row row : rows(workbook, "attachments")) {
            if (!targetType.equals(cell(row, 0).toUpperCase(Locale.ROOT))) {
                continue;
            }
            attachments.computeIfAbsent(cell(row, 1), key -> new ArrayList<>())
                    .add(new QuestionAttachmentRequest(cell(row, 2), assetStore.importFileUrl(entries, cell(row, 3)), cell(row, 4)));
        }
        return attachments;
    }

    private void writeSheet(Workbook workbook, String name, List<String> headers, List<List<String>> rows) {
        Sheet sheet = workbook.createSheet(name);
        Row header = sheet.createRow(0);
        for (int index = 0; index < headers.size(); index++) {
            header.createCell(index).setCellValue(headers.get(index));
            sheet.setColumnWidth(index, 4800);
        }
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            List<String> values = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                row.createCell(columnIndex).setCellValue(values.get(columnIndex));
            }
        }
    }

    private List<Row> rows(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            return List.of();
        }
        List<Row> rows = new ArrayList<>();
        for (int index = 1; index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row != null && !cell(row, 0).isBlank()) {
                rows.add(row);
            }
        }
        return rows;
    }

    private void forEachNode(QuestionContentTreeResponse tree, NodeConsumer consumer) {
        for (QuestionContentNodeResponse section : tree.sections()) {
            consumer.accept(section);
            section.children().forEach(consumer::accept);
        }
    }

    private void forEachQuestion(QuestionContentTreeResponse tree, QuestionConsumer consumer) {
        for (QuestionContentNodeResponse section : tree.sections()) {
            for (QuestionContentNodeResponse group : section.children()) {
                group.questions().forEach(question -> consumer.accept(group.nodeCode(), question));
            }
        }
        tree.ungroupedQuestions().forEach(question -> consumer.accept("", question));
    }

    private boolean hasSharedOptions(QuestionContentTreeResponse tree, String groupCode) {
        if (groupCode == null || groupCode.isBlank()) {
            return false;
        }
        for (QuestionContentNodeResponse section : tree.sections()) {
            for (QuestionContentNodeResponse group : section.children()) {
                if (groupCode.equals(group.nodeCode())) {
                    return !group.sharedOptions().isEmpty();
                }
            }
        }
        return false;
    }

    private String answer(QuestionResponse question) {
        return question.options().stream()
                .filter(option -> Boolean.TRUE.equals(option.correct()))
                .map(option -> option.label() == null ? "" : option.label())
                .reduce("", String::concat);
    }

    private String itemCode(QuestionResponse question) {
        return "q-" + question.id();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String cell(Row row, int index) {
        return ExcelWorkbooks.text(row, index).trim();
    }

    private int number(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }

    private String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean truthy(String value) {
        return "TRUE".equalsIgnoreCase(value) || "是".equals(value) || "1".equals(value);
    }

    private List<String> labels(String value) {
        return value == null ? List.of() : value.toUpperCase(Locale.ROOT)
                .chars()
                .mapToObj(character -> String.valueOf((char) character))
                .filter(label -> !label.isBlank())
                .distinct()
                .toList();
    }

    private interface NodeConsumer {
        void accept(QuestionContentNodeResponse node);
    }

    private interface QuestionConsumer {
        void accept(String groupCode, QuestionResponse question);
    }
}
