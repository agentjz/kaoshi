package com.kaoshi.admin.menu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("menus")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String title;
    private String path;
    private Long parentId;
    private Integer sortOrder;
    private String icon;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public Long getParentId() {
        return parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getIcon() {
        return icon;
    }
}

