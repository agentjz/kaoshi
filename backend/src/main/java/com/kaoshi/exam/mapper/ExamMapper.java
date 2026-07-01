package com.kaoshi.exam.mapper;

import com.kaoshi.exam.domain.Exam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExamMapper {
    @Select("select count(*) from papers where id = #{paperId} and status = 'ACTIVE'")
    long countActivePaperById(@Param("paperId") Long paperId);

    @Select("select name from papers where id = #{paperId}")
    String findPaperName(@Param("paperId") Long paperId);

    @Select("select count(*) from exams where #{keyword} is null or title like #{keyword}")
    long countExams(@Param("keyword") String keyword);

    @Select("""
            select *
            from exams
            where #{keyword} is null or title like #{keyword}
            order by id desc
            limit #{size} offset #{offset}
            """)
    List<Exam> findExams(@Param("keyword") String keyword, @Param("size") int size, @Param("offset") int offset);

    @Select("select * from exams where id = #{id}")
    Exam findExamById(@Param("id") Long id);

    @Insert("""
            insert into exams (paper_id, title, description, start_time, end_time, duration_minutes, status)
            values (#{paperId}, #{title}, #{description}, #{startTime}, #{endTime}, #{durationMinutes}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertExam(Exam exam);

    @Update("""
            update exams
            set paper_id = #{paperId},
                title = #{title},
                description = #{description},
                start_time = #{startTime},
                end_time = #{endTime},
                duration_minutes = #{durationMinutes},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateExam(Exam exam);

    @Select("""
            select *
            from exams
            where status = 'PUBLISHED'
            order by start_time desc, id desc
            """)
    List<Exam> findPublishedExams();

    @Select("""
            select pq.question_id as questionId,
                   q.type as type,
                   q.stem as stem,
                   pq.score as score,
                   pq.sort_order as sortOrder
            from exams e
            join paper_questions pq on pq.paper_id = e.paper_id
            join questions q on q.id = pq.question_id
            where e.id = #{examId}
            order by pq.sort_order, pq.id
            """)
    List<Map<String, Object>> findExamQuestions(@Param("examId") Long examId);

    @Select("""
            select id, option_label as label, content, sort_order as sortOrder
            from question_options
            where question_id = #{questionId}
            order by sort_order, id
            """)
    List<Map<String, Object>> findQuestionOptions(@Param("questionId") Long questionId);

    @Select("""
            select id, file_name as fileName, file_url as fileUrl, media_type as mediaType, sort_order as sortOrder
            from question_attachments
            where question_id = #{questionId}
            order by sort_order, id
            """)
    List<Map<String, Object>> findQuestionAttachments(@Param("questionId") Long questionId);

    @Select("""
            select option_label
            from question_options
            where question_id = #{questionId} and is_correct = true
            order by option_label
            """)
    List<String> findCorrectLabels(@Param("questionId") Long questionId);

    @Select("select * from exam_attempts where exam_id = #{examId} and user_id = #{userId}")
    Map<String, Object> findAttempt(@Param("examId") Long examId, @Param("userId") Long userId);

    @Insert("""
            insert into exam_attempts (exam_id, user_id, status, total_score, obtained_score, duration_seconds)
            values (#{examId}, #{userId}, 'IN_PROGRESS', 0, 0, 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAttempt(Map<String, Object> attempt);

    @Select("select * from exam_attempts where id = #{attemptId}")
    Map<String, Object> findAttemptById(@Param("attemptId") Long attemptId);

    @Insert("""
            insert into exam_answers (attempt_id, question_id, selected_labels, is_correct, score)
            values (#{attemptId}, #{questionId}, #{selectedLabels}, #{correct}, #{score})
            """)
    void insertAnswer(
            @Param("attemptId") Long attemptId,
            @Param("questionId") Long questionId,
            @Param("selectedLabels") String selectedLabels,
            @Param("correct") boolean correct,
            @Param("score") BigDecimal score
    );

    @Update("""
            update exam_attempts
            set status = 'SUBMITTED',
                submitted_at = #{submittedAt},
                total_score = #{totalScore},
                obtained_score = #{obtainedScore},
                duration_seconds = #{durationSeconds},
                updated_at = current_timestamp
            where id = #{attemptId} and status = 'IN_PROGRESS'
            """)
    int submitAttempt(
            @Param("attemptId") Long attemptId,
            @Param("submittedAt") LocalDateTime submittedAt,
            @Param("totalScore") BigDecimal totalScore,
            @Param("obtainedScore") BigDecimal obtainedScore,
            @Param("durationSeconds") int durationSeconds
    );

    @Insert("""
            insert into exam_results (attempt_id, exam_id, user_id, total_score, obtained_score, correct_count, question_count, submitted_at)
            values (#{attemptId}, #{examId}, #{userId}, #{totalScore}, #{obtainedScore}, #{correctCount}, #{questionCount}, #{submittedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertResult(Map<String, Object> result);

    @Select("""
            select r.*, e.title as examTitle
            from exam_results r
            join exams e on e.id = r.exam_id
            order by r.id desc
            """)
    List<Map<String, Object>> findResults();

    @Select("""
            select r.*, e.title as examTitle
            from exam_results r
            join exams e on e.id = r.exam_id
            where r.user_id = #{userId}
            order by r.id desc
            """)
    List<Map<String, Object>> findResultsByUser(@Param("userId") Long userId);
}

