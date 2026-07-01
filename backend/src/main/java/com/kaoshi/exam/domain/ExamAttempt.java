package com.kaoshi.exam.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("exam_attempts")
public class ExamAttempt {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long userId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private BigDecimal totalScore;
    private BigDecimal obtainedScore;
    private Integer durationSeconds;

    public Long getId() {
        return id;
    }

    public Long getExamId() {
        return examId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public BigDecimal getObtainedScore() {
        return obtainedScore;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }
}

