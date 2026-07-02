package com.kaoshi.common.excel;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ExcelWorkbooks {
    private ExcelWorkbooks() {
    }

    public static ResponseEntity<byte[]> template(String filename, String sheetName, List<String> headers, List<List<String>> rows) {
        return template(filename, List.of(new SheetData(sheetName, headers, rows)));
    }

    public static ResponseEntity<byte[]> template(String filename, List<SheetData> sheets) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (SheetData sheetData : sheets) {
                writeSheet(workbook, sheetData);
            }
            workbook.write(output);
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(output.toByteArray());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "生成导入模板失败");
        }
    }

    private static void writeSheet(Workbook workbook, SheetData sheetData) {
        Sheet sheet = workbook.createSheet(sheetData.name());
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < sheetData.headers().size(); i++) {
            headerRow.createCell(i).setCellValue(sheetData.headers().get(i));
            sheet.setColumnWidth(i, 4800);
        }
        for (int rowIndex = 0; rowIndex < sheetData.rows().size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            List<String> values = sheetData.rows().get(rowIndex);
            for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                row.createCell(columnIndex).setCellValue(values.get(columnIndex));
            }
        }
    }

    public record SheetData(String name, List<String> headers, List<List<String>> rows) {
    }

    public static String text(Row row, int index) {
        Cell cell = row == null ? null : row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numeric = cell.getNumericCellValue();
            if (numeric == Math.rint(numeric)) {
                return String.valueOf((long) numeric);
            }
            return String.valueOf(numeric);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return cell.toString().trim();
    }
}
