package com.kaoshi.question.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("question_categories")
public class QuestionCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}

