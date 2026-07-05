package com.kaoshi.common.file;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kaoshi.exam.ExamRowValues.dateTimeValue;
import static com.kaoshi.exam.ExamRowValues.longValue;
import static com.kaoshi.exam.ExamRowValues.stringValue;
import static com.kaoshi.exam.ExamRowValues.value;

@Service
public class FileAssetService {
    private final FileAssetMapper mapper;

    public FileAssetService(FileAssetMapper mapper) {
        this.mapper = mapper;
    }

    public void record(String originalName, String fileUrl, String mediaType, Long uploadedBy) {
        Map<String, Object> asset = new HashMap<>();
        asset.put("originalName", originalName);
        asset.put("fileUrl", fileUrl);
        asset.put("mediaType", mediaType);
        asset.put("usageType", "QUESTION_OR_EXAM_ATTACHMENT");
        asset.put("uploadedBy", uploadedBy);
        mapper.insert(asset);
    }

    public List<FileAssetResponse> latest() {
        return mapper.latest(100).stream()
                .map(row -> new FileAssetResponse(
                        longValue(value(row, "id")),
                        stringValue(value(row, "originalName")),
                        stringValue(value(row, "fileUrl")),
                        stringValue(value(row, "mediaType")),
                        stringValue(value(row, "usageType")),
                        stringValue(value(row, "uploadedBy")),
                        dateTimeValue(value(row, "uploadedAt"))
                ))
                .toList();
    }
}
