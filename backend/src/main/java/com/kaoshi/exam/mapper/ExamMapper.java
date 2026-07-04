package com.kaoshi.exam.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamMapper extends ExamCoreMapper, ExamPaperMapper, ExamAttemptMapper, ExamResultMapper {
}
