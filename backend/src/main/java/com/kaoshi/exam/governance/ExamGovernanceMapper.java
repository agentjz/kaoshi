package com.kaoshi.exam.governance;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExamGovernanceMapper {
    @Select("select count(*) from exams where id = #{examId}")
    int countExam(@Param("examId") Long examId);

    @Select("select count(*) from exam_participants where exam_id = #{examId}")
    int countParticipants(@Param("examId") Long examId);

    @Select("""
            select count(*)
            from exam_participants
            where exam_id = #{examId}
              and user_id = #{userId}
              and status = 'ASSIGNED'
            """)
    int countAssignedParticipant(@Param("examId") Long examId, @Param("userId") Long userId);

    @Select("select department_id from users where id = #{userId} and deleted_at is null")
    Long findUserDepartmentId(@Param("userId") Long userId);

    @Select("select department_id from exam_departments where exam_id = #{examId}")
    List<Long> findExamDepartmentIds(@Param("examId") Long examId);

    @Select("""
            select ep.user_id as userId,
                   u.username,
                   u.display_name as displayName,
                   d.name as departmentName,
                   ep.status,
                   coalesce(epa.extra_minutes, 0) as extraMinutes,
                   coalesce(epa.extra_attempts, 0) as extraAttempts,
                   epa.reason,
                   ep.assigned_at as assignedAt
            from exam_participants ep
            join users u on u.id = ep.user_id
            left join departments d on d.id = u.department_id
            left join exam_participant_allowances epa on epa.exam_id = ep.exam_id and epa.user_id = ep.user_id
            where ep.exam_id = #{examId}
            order by ep.assigned_at desc, ep.user_id desc
            """)
    List<Map<String, Object>> findParticipants(@Param("examId") Long examId);

    @Delete("delete from exam_participants where exam_id = #{examId}")
    void deleteParticipants(@Param("examId") Long examId);

    @Insert("""
            insert into exam_participants (exam_id, user_id, status, assigned_by)
            values (#{examId}, #{userId}, 'ASSIGNED', #{actorUserId})
            on duplicate key update
              status = 'ASSIGNED',
              assigned_by = values(assigned_by),
              assigned_at = current_timestamp
            """)
    void upsertParticipant(@Param("examId") Long examId, @Param("userId") Long userId, @Param("actorUserId") Long actorUserId);

    @Select("select count(*) from users where id = #{userId} and deleted_at is null and status = 'ACTIVE'")
    int countActiveUser(@Param("userId") Long userId);

    @Select("""
            select coalesce(extra_minutes, 0) as extraMinutes,
                   coalesce(extra_attempts, 0) as extraAttempts,
                   reason
            from exam_participant_allowances
            where exam_id = #{examId} and user_id = #{userId}
            """)
    Map<String, Object> findAllowance(@Param("examId") Long examId, @Param("userId") Long userId);

    @Insert("""
            insert into exam_participant_allowances (exam_id, user_id, extra_minutes, extra_attempts, reason, updated_by)
            values (#{examId}, #{userId}, #{extraMinutes}, #{extraAttempts}, #{reason}, #{actorUserId})
            on duplicate key update
              extra_minutes = values(extra_minutes),
              extra_attempts = values(extra_attempts),
              reason = values(reason),
              updated_by = values(updated_by),
              updated_at = current_timestamp
            """)
    void upsertAllowance(
            @Param("examId") Long examId,
            @Param("userId") Long userId,
            @Param("extraMinutes") Integer extraMinutes,
            @Param("extraAttempts") Integer extraAttempts,
            @Param("reason") String reason,
            @Param("actorUserId") Long actorUserId
    );

    @Update("""
            update exam_participant_allowances
            set extra_attempts = extra_attempts + 1,
                reason = #{reason},
                updated_by = #{actorUserId},
                updated_at = current_timestamp
            where exam_id = #{examId} and user_id = #{userId}
            """)
    int incrementExtraAttempt(@Param("examId") Long examId, @Param("userId") Long userId, @Param("reason") String reason, @Param("actorUserId") Long actorUserId);

    @Select("""
            select exam_id as examId,
                   visible_to_students as visibleToStudents,
                   show_answers as showAnswers,
                   show_analysis as showAnalysis,
                   release_time as releaseTime,
                   updated_at as updatedAt
            from exam_result_policies
            where exam_id = #{examId}
            """)
    Map<String, Object> findResultPolicy(@Param("examId") Long examId);

    @Insert("""
            insert into exam_result_policies (exam_id, visible_to_students, show_answers, show_analysis, release_time, updated_by)
            values (#{examId}, #{visibleToStudents}, #{showAnswers}, #{showAnalysis}, #{releaseTime}, #{actorUserId})
            on duplicate key update
              visible_to_students = values(visible_to_students),
              show_answers = values(show_answers),
              show_analysis = values(show_analysis),
              release_time = values(release_time),
              updated_by = values(updated_by),
              updated_at = current_timestamp
            """)
    void upsertResultPolicy(
            @Param("examId") Long examId,
            @Param("visibleToStudents") Boolean visibleToStudents,
            @Param("showAnswers") Boolean showAnswers,
            @Param("showAnalysis") Boolean showAnalysis,
            @Param("releaseTime") LocalDateTime releaseTime,
            @Param("actorUserId") Long actorUserId
    );

    @Select("""
            select count(*)
            from exam_results
            where exam_id = #{examId}
            """)
    int countSubmitted(@Param("examId") Long examId);

    @Select("""
            select count(*)
            from exam_results
            where exam_id = #{examId} and grading_status = 'PENDING_REVIEW'
            """)
    int countPendingReview(@Param("examId") Long examId);

    @Select("""
            select coalesce(avg(obtained_score), 0)
            from exam_results
            where exam_id = #{examId} and grading_status = 'FINAL'
            """)
    BigDecimal averageScore(@Param("examId") Long examId);

    @Select("""
            select coalesce(max(obtained_score), 0)
            from exam_results
            where exam_id = #{examId} and grading_status = 'FINAL'
            """)
    BigDecimal maxScore(@Param("examId") Long examId);

    @Select("""
            select coalesce(min(obtained_score), 0)
            from exam_results
            where exam_id = #{examId} and grading_status = 'FINAL'
            """)
    BigDecimal minScore(@Param("examId") Long examId);

    @Select("""
            select count(*)
            from exam_results r
            join exams e on e.id = r.exam_id
            where r.exam_id = #{examId}
              and r.grading_status = 'FINAL'
              and r.obtained_score >= e.qualify_score
            """)
    int countPassed(@Param("examId") Long examId);

    @Insert("""
            insert into exam_attempt_events (exam_id, attempt_id, user_id, actor_user_id, action, reason)
            values (#{examId}, #{attemptId}, #{userId}, #{actorUserId}, #{action}, #{reason})
            """)
    void insertEvent(
            @Param("examId") Long examId,
            @Param("attemptId") Long attemptId,
            @Param("userId") Long userId,
            @Param("actorUserId") Long actorUserId,
            @Param("action") String action,
            @Param("reason") String reason
    );

    @Select("""
            select e.id,
                   e.exam_id as examId,
                   e.attempt_id as attemptId,
                   e.user_id as userId,
                   u.username,
                   actor.username as actorUsername,
                   e.action,
                   e.reason,
                   e.created_at as createdAt
            from exam_attempt_events e
            left join users u on u.id = e.user_id
            left join users actor on actor.id = e.actor_user_id
            where e.exam_id = #{examId}
            order by e.id desc
            limit 100
            """)
    List<Map<String, Object>> findEvents(@Param("examId") Long examId);

    @Select("""
            select exam_id as examId,
                   require_fullscreen as requireFullscreen,
                   forbid_copy_paste as forbidCopyPaste,
                   track_focus_loss as trackFocusLoss,
                   max_focus_loss_count as maxFocusLossCount,
                   device_check_required as deviceCheckRequired,
                   updated_at as updatedAt
            from exam_security_policies
            where exam_id = #{examId}
            """)
    Map<String, Object> findSecurityPolicy(@Param("examId") Long examId);

    @Insert("""
            insert into exam_security_policies (exam_id, require_fullscreen, forbid_copy_paste, track_focus_loss, max_focus_loss_count, device_check_required, updated_by)
            values (#{examId}, #{requireFullscreen}, #{forbidCopyPaste}, #{trackFocusLoss}, #{maxFocusLossCount}, #{deviceCheckRequired}, #{actorUserId})
            on duplicate key update
              require_fullscreen = values(require_fullscreen),
              forbid_copy_paste = values(forbid_copy_paste),
              track_focus_loss = values(track_focus_loss),
              max_focus_loss_count = values(max_focus_loss_count),
              device_check_required = values(device_check_required),
              updated_by = values(updated_by),
              updated_at = current_timestamp
            """)
    void upsertSecurityPolicy(
            @Param("examId") Long examId,
            @Param("requireFullscreen") Boolean requireFullscreen,
            @Param("forbidCopyPaste") Boolean forbidCopyPaste,
            @Param("trackFocusLoss") Boolean trackFocusLoss,
            @Param("maxFocusLossCount") Integer maxFocusLossCount,
            @Param("deviceCheckRequired") Boolean deviceCheckRequired,
            @Param("actorUserId") Long actorUserId
    );

    @Insert("""
            insert into exam_security_events (exam_id, attempt_id, user_id, event_type, severity, detail)
            values (#{examId}, #{attemptId}, #{userId}, #{eventType}, #{severity}, #{detail})
            """)
    void insertSecurityEvent(
            @Param("examId") Long examId,
            @Param("attemptId") Long attemptId,
            @Param("userId") Long userId,
            @Param("eventType") String eventType,
            @Param("severity") String severity,
            @Param("detail") String detail
    );

    @Select("""
            select e.id,
                   e.exam_id as examId,
                   e.attempt_id as attemptId,
                   e.user_id as userId,
                   u.username,
                   e.event_type as eventType,
                   e.severity,
                   e.detail,
                   e.occurred_at as occurredAt
            from exam_security_events e
            left join users u on u.id = e.user_id
            where e.exam_id = #{examId}
            order by e.id desc
            limit 100
            """)
    List<Map<String, Object>> findSecurityEvents(@Param("examId") Long examId);

    @Select("""
            select id, exam_id as examId, title, description, max_score as maxScore, sort_order as sortOrder
            from exam_review_rubrics
            where exam_id = #{examId}
            order by sort_order asc, id asc
            """)
    List<Map<String, Object>> findRubrics(@Param("examId") Long examId);

    @Delete("delete from exam_review_rubrics where exam_id = #{examId}")
    void deleteRubrics(@Param("examId") Long examId);

    @Insert("""
            insert into exam_review_rubrics (exam_id, title, description, max_score, sort_order)
            values (#{examId}, #{title}, #{description}, #{maxScore}, #{sortOrder})
            """)
    void insertRubric(
            @Param("examId") Long examId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("maxScore") BigDecimal maxScore,
            @Param("sortOrder") Integer sortOrder
    );

    @Insert("""
            insert into exam_review_tasks (result_id, exam_id)
            select r.id, r.exam_id
            from exam_results r
            where r.exam_id = #{examId}
              and r.grading_status = 'PENDING_REVIEW'
              and not exists (select 1 from exam_review_tasks t where t.result_id = r.id)
            """)
    int generateReviewTasks(@Param("examId") Long examId);

    @Select("""
            select t.id,
                   t.result_id as resultId,
                   t.exam_id as examId,
                   e.title as examTitle,
                   t.reviewer_id as reviewerId,
                   reviewer.username as reviewerUsername,
                   t.status,
                   student.display_name as studentName,
                   t.assigned_at as assignedAt,
                   t.completed_at as completedAt,
                   t.created_at as createdAt
            from exam_review_tasks t
            join exams e on e.id = t.exam_id
            join exam_results r on r.id = t.result_id
            join users student on student.id = r.user_id
            left join users reviewer on reviewer.id = t.reviewer_id
            where t.exam_id = #{examId}
            order by t.id desc
            """)
    List<Map<String, Object>> findReviewTasks(@Param("examId") Long examId);

    @Update("""
            update exam_review_tasks
            set reviewer_id = #{actorUserId},
                status = 'IN_PROGRESS',
                assigned_at = coalesce(assigned_at, current_timestamp),
                updated_at = current_timestamp
            where id = #{taskId}
            """)
    int claimReviewTask(@Param("taskId") Long taskId, @Param("actorUserId") Long actorUserId);

    @Update("""
            update exam_review_tasks
            set status = #{status},
                completed_at = case when #{status} = 'COMPLETED' then current_timestamp else completed_at end,
                updated_at = current_timestamp
            where id = #{taskId}
            """)
    int updateReviewTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    @Select("select exam_id from exam_review_tasks where id = #{taskId}")
    Long findReviewTaskExamId(@Param("taskId") Long taskId);

    @Select("select result_id from exam_review_tasks where id = #{taskId}")
    Long findReviewTaskResultId(@Param("taskId") Long taskId);

    @Insert("""
            insert into exam_review_rechecks (task_id, result_id, requested_by, reason)
            values (#{taskId}, #{resultId}, #{actorUserId}, #{reason})
            """)
    void insertReviewRecheck(@Param("taskId") Long taskId, @Param("resultId") Long resultId, @Param("actorUserId") Long actorUserId, @Param("reason") String reason);

    @Update("""
            update exam_review_rechecks
            set status = #{status},
                resolution = #{resolution},
                resolved_at = case when #{status} in ('APPROVED', 'REJECTED', 'RESOLVED') then current_timestamp else resolved_at end
            where id = #{recheckId}
            """)
    int updateReviewRecheck(@Param("recheckId") Long recheckId, @Param("status") String status, @Param("resolution") String resolution);

    @Select("""
            select c.id,
                   c.task_id as taskId,
                   c.result_id as resultId,
                   u.username as requestedBy,
                   c.status,
                   c.reason,
                   c.resolution,
                   c.created_at as createdAt,
                   c.resolved_at as resolvedAt
            from exam_review_rechecks c
            join users u on u.id = c.requested_by
            join exam_review_tasks t on t.id = c.task_id
            where t.exam_id = #{examId}
            order by c.id desc
            """)
    List<Map<String, Object>> findReviewRechecks(@Param("examId") Long examId);
}
