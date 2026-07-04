package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
class QuestionBankPackageZipCodec {
    byte[] write(byte[] manifest, byte[] workbook, Map<String, Path> assets) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            writeEntry(zip, "manifest.json", manifest);
            writeEntry(zip, "content.xlsx", workbook);
            for (Map.Entry<String, Path> entry : assets.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                Files.copy(entry.getValue(), zip);
                zip.closeEntry();
            }
            zip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "生成题库包失败");
        }
    }

    QuestionBankPackageEntries read(MultipartFile file) {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName().replace("\\", "/");
                if (name.contains("..")) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "题库包路径不合法");
                }
                entries.put(name, zip.readAllBytes());
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "读取题库包失败");
        }
        return new QuestionBankPackageEntries(entries);
    }

    private void writeEntry(ZipOutputStream zip, String name, byte[] bytes) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }
}
