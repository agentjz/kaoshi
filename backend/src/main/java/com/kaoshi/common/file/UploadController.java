package com.kaoshi.common.file;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.security.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/files")
@PreAuthorize("hasAuthority('system:admin')")
public class UploadController {
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "mp3", "wav", "ogg", "mp4", "pdf"
    );

    private final Path uploadRoot;
    private final FileAssetService fileAssetService;

    public UploadController(@Value("${kaoshi.upload.dir:uploads}") String uploadDir, FileAssetService fileAssetService) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.fileAssetService = fileAssetService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> upload(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal AuthUser user) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "上传文件不能为空");
        }
        String originalName = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension = extension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "文件类型不允许上传");
        }
        String relativeDir = LocalDate.now().toString().replace("-", "/");
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(relativeDir).resolve(storedName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "文件路径不合法");
        }
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }
        String fileUrl = "/uploads/" + relativeDir + "/" + storedName;
        String mediaType = mediaType(extension);
        fileAssetService.record(originalName, fileUrl, mediaType, user == null ? null : user.id());
        return ApiResponse.ok(new FileUploadResponse(
                originalName,
                fileUrl,
                mediaType
        ));
    }

    @GetMapping
    public ApiResponse<List<FileAssetResponse>> latest() {
        return ApiResponse.ok(fileAssetService.latest());
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "文件类型不能为空");
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String mediaType(String extension) {
        if (List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            return "IMAGE";
        }
        if (List.of("mp3", "wav", "ogg").contains(extension)) {
            return "AUDIO";
        }
        if ("mp4".equals(extension)) {
            return "VIDEO";
        }
        return "FILE";
    }
}
