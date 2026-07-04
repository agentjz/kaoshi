package com.kaoshi.question.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bankId;
    private Long nodeId;
    private String type;
    private String stem;
    private String sectionCode;
    private String sectionTitle;
    private Integer sectionSortOrder;
    private String groupCode;
    private String groupTitle;
    private String groupDirection;
    private String groupMaterial;
    private Integer groupSortOrder;
    private String itemLabel;
    private String itemStem;
    private String analysis;
    private String difficulty;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public Integer getSectionSortOrder() {
        return sectionSortOrder;
    }

    public void setSectionSortOrder(Integer sectionSortOrder) {
        this.sectionSortOrder = sectionSortOrder;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDirection() {
        return groupDirection;
    }

    public void setGroupDirection(String groupDirection) {
        this.groupDirection = groupDirection;
    }

    public String getGroupMaterial() {
        return groupMaterial;
    }

    public void setGroupMaterial(String groupMaterial) {
        this.groupMaterial = groupMaterial;
    }

    public Integer getGroupSortOrder() {
        return groupSortOrder;
    }

    public void setGroupSortOrder(Integer groupSortOrder) {
        this.groupSortOrder = groupSortOrder;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getItemStem() {
        return itemStem;
    }

    public void setItemStem(String itemStem) {
        this.itemStem = itemStem;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

