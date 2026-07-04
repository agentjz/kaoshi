package com.kaoshi.exam.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

interface ExamAttemptMapper {
    String ATTEMPT_NODE_FIELDS = """
                   case when section_node.id is null then null else section_node.node_code end as sectionCode,
                   case when section_node.id is null then null else section_node.title end as sectionTitle,
                   case when section_node.id is null then 0 else section_node.sort_order end as sectionSortOrder,
                   case when group_node.id is null then null else group_node.node_code end as groupCode,
                   case when group_node.id is null then null else group_node.title end as groupTitle,
                   case when group_node.id is null then null else group_node.direction end as groupDirection,
                   case when group_node.id is null then null else group_node.material end as groupMaterial,
                   case when group_node.id is null then 0 else group_node.sort_order end as groupSortOrder
            """;

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

    @Select("select count(*) from exam_attempts where exam_id = #{examId} and user_id = #{userId} and status = 'SUBMITTED'")
    int countSubmittedAttempts(@Param("examId") Long examId, @Param("userId") Long userId);

    @Insert("""
            insert into exam_attempts (exam_id, user_id, status, total_score, obtained_score, duration_seconds)
            values (#{examId}, #{userId}, 'IN_PROGRESS', 0, 0, 0)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAttempt(Map<String, Object> attempt);

    @Select("select count(*) from exam_attempt_questions where attempt_id = #{attemptId}")
    int countAttemptQuestions(@Param("attemptId") Long attemptId);

    @Select("select * from exam_published_nodes where id = #{publishedNodeId}")
    Map<String, Object> findPublishedNode(@Param("publishedNodeId") Long publishedNodeId);

    @Select("""
            select *
            from exam_attempt_nodes
            where attempt_id = #{attemptId}
              and source_node_id = #{sourceNodeId}
            """)
    Map<String, Object> findAttemptNodeBySource(@Param("attemptId") Long attemptId, @Param("sourceNodeId") Long sourceNodeId);

    @Insert("""
            insert into exam_attempt_nodes (attempt_id, source_node_id, parent_id, node_code, node_type, title, direction, material, sort_order)
            values (#{attemptId}, #{sourceNodeId}, #{parentId}, #{nodeCode}, #{nodeType}, #{title}, #{direction}, #{material}, #{sortOrder})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAttemptNode(Map<String, Object> node);

    @Insert("""
            insert into exam_attempt_node_options (attempt_node_id, option_label, content, sort_order)
            select #{attemptNodeId}, option_label, content, sort_order
            from exam_published_node_options
            where published_node_id = #{publishedNodeId}
            order by sort_order, id
            """)
    void copyAttemptNodeOptions(@Param("attemptNodeId") Long attemptNodeId, @Param("publishedNodeId") Long publishedNodeId);

    @Insert("""
            insert into exam_attempt_node_attachments (attempt_node_id, file_name, file_url, media_type, sort_order)
            select #{attemptNodeId}, file_name, file_url, media_type, sort_order
            from exam_published_node_attachments
            where published_node_id = #{publishedNodeId}
            order by sort_order, id
            """)
    void copyAttemptNodeAttachments(@Param("attemptNodeId") Long attemptNodeId, @Param("publishedNodeId") Long publishedNodeId);

    @Insert("""
            insert into exam_attempt_questions (
              attempt_id, attempt_node_id, published_question_id, source_question_id, type, stem,
              item_label, item_stem, analysis, score, sort_order, display_order
            )
            values (
              #{attemptId}, #{attemptNodeId}, #{publishedQuestionId}, #{sourceQuestionId}, #{type}, #{stem},
              #{itemLabel}, #{itemStem}, #{analysis}, #{score}, #{sortOrder}, #{displayOrder}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAttemptQuestion(Map<String, Object> question);

    @Insert("""
            insert into exam_attempt_options (attempt_question_id, option_label, content, is_correct, sort_order)
            select #{attemptQuestionId}, option_label, content, is_correct, sort_order
            from exam_published_options
            where published_question_id = #{publishedQuestionId}
            order by sort_order, id
            """)
    void copyAttemptOptions(@Param("attemptQuestionId") Long attemptQuestionId, @Param("publishedQuestionId") Long publishedQuestionId);

    @Insert("""
            insert into exam_attempt_answer_labels (attempt_question_id, answer_label, sort_order)
            select #{attemptQuestionId}, answer_label, sort_order
            from exam_published_answer_labels
            where published_question_id = #{publishedQuestionId}
            order by sort_order, id
            """)
    void copyAttemptAnswerLabels(@Param("attemptQuestionId") Long attemptQuestionId, @Param("publishedQuestionId") Long publishedQuestionId);

    @Insert("""
            insert into exam_attempt_attachments (attempt_question_id, file_name, file_url, media_type, sort_order)
            select #{attemptQuestionId}, file_name, file_url, media_type, sort_order
            from exam_published_attachments
            where published_question_id = #{publishedQuestionId}
            order by sort_order, id
            """)
    void copyAttemptAttachments(@Param("attemptQuestionId") Long attemptQuestionId, @Param("publishedQuestionId") Long publishedQuestionId);

    @Select("""
            select aq.*,
            """ + ATTEMPT_NODE_FIELDS + """
                   ,
                   ea.selected_labels as selectedLabels,
                   ea.answer_text as answerText
            from exam_attempt_questions aq
            left join exam_attempt_nodes group_node on group_node.id = aq.attempt_node_id
            left join exam_attempt_nodes section_node on section_node.id = group_node.parent_id
            left join exam_answers ea on ea.attempt_question_id = aq.id
            where aq.attempt_id = #{attemptId}
            order by aq.display_order, aq.id
            """)
    List<Map<String, Object>> findAttemptQuestions(@Param("attemptId") Long attemptId);

    @Select("""
            select id, option_label as label, content, sort_order as sortOrder
            from exam_attempt_options
            where attempt_question_id = #{attemptQuestionId}
            union all
            select ano.id, ano.option_label as label, ano.content, ano.sort_order as sortOrder
            from exam_attempt_questions aq
            join exam_attempt_node_options ano on ano.attempt_node_id = aq.attempt_node_id
            where aq.id = #{attemptQuestionId}
              and not exists (select 1 from exam_attempt_options ao where ao.attempt_question_id = aq.id)
            order by sortOrder, id
            """)
    List<Map<String, Object>> findAttemptOptions(@Param("attemptQuestionId") Long attemptQuestionId);

    @Select("""
            select id, file_name as fileName, file_url as fileUrl, media_type as mediaType, sort_order as sortOrder
            from exam_attempt_attachments
            where attempt_question_id = #{attemptQuestionId}
            union all
            select ana.id, ana.file_name as fileName, ana.file_url as fileUrl, ana.media_type as mediaType, ana.sort_order as sortOrder
            from exam_attempt_questions aq
            join exam_attempt_node_attachments ana on ana.attempt_node_id = aq.attempt_node_id
            where aq.id = #{attemptQuestionId}
            union all
            select sna.id, sna.file_name as fileName, sna.file_url as fileUrl, sna.media_type as mediaType, sna.sort_order as sortOrder
            from exam_attempt_questions aq
            join exam_attempt_nodes group_node on group_node.id = aq.attempt_node_id
            join exam_attempt_node_attachments sna on sna.attempt_node_id = group_node.parent_id
            where aq.id = #{attemptQuestionId}
            order by sortOrder, id
            """)
    List<Map<String, Object>> findAttemptAttachments(@Param("attemptQuestionId") Long attemptQuestionId);

    @Select("""
            select option_label
            from exam_attempt_options
            where attempt_question_id = #{attemptQuestionId} and is_correct = true
            union
            select answer_label
            from exam_attempt_answer_labels
            where attempt_question_id = #{attemptQuestionId}
            order by option_label
            """)
    List<String> findAttemptCorrectLabels(@Param("attemptQuestionId") Long attemptQuestionId);

    @Insert("""
            insert into exam_answers (attempt_id, attempt_question_id, selected_labels, answer_text, is_correct, score)
            values (#{attemptId}, #{attemptQuestionId}, #{selectedLabels}, #{answerText}, #{correct}, #{score})
            on duplicate key update
              selected_labels = values(selected_labels),
              answer_text = values(answer_text),
              is_correct = values(is_correct),
              score = values(score),
              updated_at = current_timestamp
            """)
    void upsertAnswer(
            @Param("attemptId") Long attemptId,
            @Param("attemptQuestionId") Long attemptQuestionId,
            @Param("selectedLabels") String selectedLabels,
            @Param("answerText") String answerText,
            @Param("correct") boolean correct,
            @Param("score") BigDecimal score
    );

    @Insert("""
            insert into exam_answers (attempt_id, attempt_question_id, selected_labels, answer_text, is_correct, score)
            values (#{attemptId}, #{attemptQuestionId}, null, #{answerText}, null, 0)
            on duplicate key update
              answer_text = values(answer_text),
              updated_at = current_timestamp
            """)
    void upsertWritingAnswer(
            @Param("attemptId") Long attemptId,
            @Param("attemptQuestionId") Long attemptQuestionId,
            @Param("answerText") String answerText
    );

    @Select("select selected_labels from exam_answers where attempt_question_id = #{attemptQuestionId}")
    String findSelectedLabels(@Param("attemptQuestionId") Long attemptQuestionId);

    @Select("select answer_text from exam_answers where attempt_question_id = #{attemptQuestionId}")
    String findAnswerText(@Param("attemptQuestionId") Long attemptQuestionId);

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

}
