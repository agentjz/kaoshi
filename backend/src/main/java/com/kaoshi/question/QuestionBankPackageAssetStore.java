package com.kaoshi.question;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.question.dto.QuestionAttachmentResponse;
import com.kaoshi.question.dto.QuestionContentNodeResponse;
import com.kaoshi.question.dto.QuestionContentTreeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
class QuestionBankPackageAssetStore {
    private final Path uploadRoot;

    QuestionBankPackageAssetStore(@Value("${kaoshi.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    Map<String, Path> exportAssets(QuestionContentTreeResponse tree) {
        Map<String, Path> assets = new LinkedHashMap<>();
        forEachAttachment(tree, attachment -> collectAsset(assets, attachment.fileUrl()));
        return assets;
    }

    String packageFileUrl(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            return "assets/" + fileUrl.substring("/uploads/".length()).replace("\\", "/");
        }
        return fileUrl == null ? "" : fileUrl;
    }

    String importFileUrl(QuestionBankPackageEntries entries, String fileUrl) {
        if (!fileUrl.startsWith("assets/")) {
            return fileUrl;
        }
        byte[] bytes = entries.required(fileUrl);
        String extension = extension(fileUrl);
        String relative = "imported/" + UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path target = uploadRoot.resolve(relative).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "附件路径不合法");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "保存题库包附件失败");
        }
        return "/uploads/" + relative.replace("\\", "/");
    }

    private void collectAsset(Map<String, Path> assets, String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        Path source = uploadRoot.resolve(fileUrl.substring("/uploads/".length())).normalize();
        if (source.startsWith(uploadRoot) && Files.exists(source)) {
            assets.put(packageFileUrl(fileUrl), source);
        }
    }

    private void forEachAttachment(QuestionContentTreeResponse tree, AttachmentConsumer consumer) {
        for (QuestionContentNodeResponse section : tree.sections()) {
            section.attachments().forEach(consumer::accept);
            for (QuestionContentNodeResponse group : section.children()) {
                group.attachments().forEach(consumer::accept);
                group.questions().forEach(question -> question.attachments().forEach(consumer::accept));
            }
        }
        tree.ungroupedQuestions().forEach(question -> question.attachments().forEach(consumer::accept));
    }

    private String extension(String value) {
        int dot = value.lastIndexOf('.');
        return dot < 0 ? "" : value.substring(dot + 1);
    }

    private interface AttachmentConsumer {
        void accept(QuestionAttachmentResponse attachment);
    }
}
