package com.kaoshi.question.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("question_attachments")
public class QuestionAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String fileName;
    private String fileUrl;
    private String mediaType;
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}

