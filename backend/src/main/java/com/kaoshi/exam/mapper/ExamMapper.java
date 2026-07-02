package com.kaoshi.exam.mapper;

import com.kaoshi.exam.domain.Exam;
import org.apache.ibatis.annotations.Delete;
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

    @Select("select total_score from papers where id = #{paperId}")
    BigDecimal findPaperTotalScore(@Param("paperId") Long paperId);

    @Select("select department_id from users where id = #{userId} and deleted_at is null")
    Long findUserDepartmentId(@Param("userId") Long userId);

    @Select("select count(*) from departments where id = #{departmentId} and status = 'ACTIVE'")
    int countActiveDepartmentById(@Param("departmentId") Long departmentId);

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
            insert into exams (paper_id, title, description, qualify_score, start_time, end_time, duration_minutes, time_limit, attempt_limit, display_mode, open_type, status)
            values (#{paperId}, #{title}, #{description}, #{qualifyScore}, #{startTime}, #{endTime}, #{durationMinutes}, #{timeLimit}, #{attemptLimit}, #{displayMode}, #{openType}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertExam(Exam exam);

    @Update("""
            update exams
            set paper_id = #{paperId},
                title = #{title},
                description = #{description},
                qualify_score = #{qualifyScore},
                start_time = #{startTime},
                end_time = #{endTime},
                duration_minutes = #{durationMinutes},
                time_limit = #{timeLimit},
                attempt_limit = #{attemptLimit},
                display_mode = #{displayMode},
                open_type = #{openType},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateExam(Exam exam);

    @Delete("delete from exam_departments where exam_id = #{examId}")
    void deleteExamDepartments(@Param("examId") Long examId);

    @Insert("""
            insert into exam_departments (exam_id, department_id)
            values (#{examId}, #{departmentId})
            """)
    void insertExamDepartment(@Param("examId") Long examId, @Param("departmentId") Long departmentId);

    @Select("""
            select department_id
            from exam_departments
            where exam_id = #{examId}
            order by department_id
            """)
    List<Long> findExamDepartmentIds(@Param("examId") Long examId);

    @Select("""
            select *
            from exams
            where status = 'PUBLISHED'
            order by start_time desc, id desc
            """)
    List<Exam> findPublishedExams();

    @Select("""
            select e.*
            from exams e
            where e.status = 'PUBLISHED'
              and (
                e.open_type = 'PUBLIC'
                or exists (
                  select 1
                  from exam_departments ed
                  where ed.exam_id = e.id
                    and ed.department_id = #{departmentId}
                )
              )
            order by e.start_time desc, e.id desc
            """)
    List<Exam> findPublishedExamsByDepartment(@Param("departmentId") Long departmentId);

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

    @Select("""
            select *
            from exam_attempts
            where exam_id = #{examId}
              and user_id = #{userId}
              and status = 'IN_PROGRESS'
            order by id desc
            limit 1
            """)
    Map<String, Object> findInProgressAttempt(@Param("examId") Long examId, @Param("userId") Long userId);

    @Select("""
            select count(*)
            from exam_attempts
            where exam_id = #{examId}
              and user_id = #{userId}
              and status = 'SUBMITTED'
            """)
    int countSubmittedAttempts(@Param("examId") Long examId, @Param("userId") Long userId);

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

    @Select("""
            select r.*, e.title as examTitle
            from exam_results r
            join exams e on e.id = r.exam_id
            where r.id = #{resultId}
            """)
    Map<String, Object> findResultById(@Param("resultId") Long resultId);

    @Select("""
            select ea.question_id as questionId,
                   q.type as type,
                   q.stem as stem,
                   q.analysis as analysis,
                   pq.score as score,
                   ea.score as obtainedScore,
                   pq.sort_order as sortOrder,
                   ea.selected_labels as selectedLabels,
                   ea.is_correct as correct
            from exam_answers ea
            join exam_attempts a on a.id = ea.attempt_id
            join exams e on e.id = a.exam_id
            join paper_questions pq on pq.paper_id = e.paper_id and pq.question_id = ea.question_id
            join questions q on q.id = ea.question_id
            where ea.attempt_id = #{attemptId}
            order by pq.sort_order, pq.id
            """)
    List<Map<String, Object>> findResultQuestions(@Param("attemptId") Long attemptId);
}
