package com.kaoshi.paper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("paper_categories")
public class PaperCategory {
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

