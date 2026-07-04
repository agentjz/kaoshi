package com.kaoshi.exam.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

interface ExamPaperMapper {
    String SOURCE_NODE_FIELDS = """
                   case when section_node.id is null then null else section_node.node_code end as sectionCode,
                   case when section_node.id is null then null else section_node.title end as sectionTitle,
                   case when section_node.id is null then 0 else section_node.sort_order end as sectionSortOrder,
                   case when group_node.id is null then null else group_node.node_code end as groupCode,
                   case when group_node.id is null then null else group_node.title end as groupTitle,
                   case when group_node.id is null then null else group_node.direction end as groupDirection,
                   case when group_node.id is null then null else group_node.material end as groupMaterial,
                   case when group_node.id is null then 0 else group_node.sort_order end as groupSortOrder
            """;

    String DRAFT_NODE_FIELDS = """
                   case when section_node.id is null then null else section_node.node_code end as sectionCode,
                   case when section_node.id is null then null else section_node.title end as sectionTitle,
                   case when section_node.id is null then 0 else section_node.sort_order end as sectionSortOrder,
                   case when group_node.id is null then null else group_node.node_code end as groupCode,
                   case when group_node.id is null then null else group_node.title end as groupTitle,
                   case when group_node.id is null then null else group_node.direction end as groupDirection,
                   case when group_node.id is null then null else group_node.material end as groupMaterial,
                   case when group_node.id is null then 0 else group_node.sort_order end as groupSortOrder
            """;

    String PUBLISHED_NODE_FIELDS = """
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
            select q.id as questionId,
                   q.bank_id as bankId,
                   b.name as bankName,
                   q.node_id as sourceNodeId,
                   q.type,
                   q.stem,
            """ + SOURCE_NODE_FIELDS + """
                   ,
                   q.item_label as itemLabel,
                   q.item_stem as itemStem,
                   q.analysis,
                   q.status
            from questions q
            join question_banks b on b.id = q.bank_id
            left join question_nodes group_node on group_node.id = q.node_id
            left join question_nodes section_node on section_node.id = group_node.parent_id
            where q.bank_id = #{bankId}
              and q.type = #{type}
              and q.status = 'ACTIVE'
            order by case when section_node.id is null then 0 else section_node.sort_order end,
                     case when group_node.id is null then 0 else group_node.sort_order end,
                     q.id
            limit #{limit}
            """)
    List<Map<String, Object>> findQuestionsForPublish(
            @Param("bankId") Long bankId,
            @Param("type") String type,
            @Param("limit") int limit
    );

    @Select("""
            select *
            from question_nodes
            where id = #{nodeId}
            """)
    Map<String, Object> findSourceNode(@Param("nodeId") Long nodeId);

    @Select("""
            select *
            from exam_draft_nodes
            where exam_id = #{examId}
              and source_node_id = #{sourceNodeId}
            """)
    Map<String, Object> findDraftNodeBySource(@Param("examId") Long examId, @Param("sourceNodeId") Long sourceNodeId);

    @Insert("""
            insert into exam_draft_nodes (exam_id, source_node_id, parent_id, node_code, node_type, title, direction, material, sort_order)
            values (#{examId}, #{sourceNodeId}, #{parentId}, #{nodeCode}, #{nodeType}, #{title}, #{direction}, #{material}, #{sortOrder})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDraftNode(Map<String, Object> node);

    @Insert("""
            insert into exam_draft_node_options (draft_node_id, option_label, content, sort_order)
            select #{draftNodeId}, option_label, content, sort_order
            from question_node_options
            where node_id = #{sourceNodeId}
            order by sort_order, id
            """)
    void copyDraftNodeOptionsFromSource(@Param("draftNodeId") Long draftNodeId, @Param("sourceNodeId") Long sourceNodeId);

    @Insert("""
            insert into exam_draft_node_attachments (draft_node_id, file_name, file_url, media_type, sort_order)
            select #{draftNodeId}, file_name, file_url, media_type, sort_order
            from question_node_attachments
            where node_id = #{sourceNodeId}
            order by sort_order, id
            """)
    void copyDraftNodeAttachmentsFromSource(@Param("draftNodeId") Long draftNodeId, @Param("sourceNodeId") Long sourceNodeId);

    @Delete("delete from exam_draft_attachments where draft_question_id in (select id from exam_draft_questions where exam_id = #{examId})")
    void deleteDraftAttachments(@Param("examId") Long examId);

    @Delete("delete from exam_draft_options where draft_question_id in (select id from exam_draft_questions where exam_id = #{examId})")
    void deleteDraftOptions(@Param("examId") Long examId);

    @Delete("delete from exam_draft_answer_labels where draft_question_id in (select id from exam_draft_questions where exam_id = #{examId})")
    void deleteDraftAnswerLabels(@Param("examId") Long examId);

    @Delete("delete from exam_draft_questions where exam_id = #{examId}")
    void deleteDraftQuestions(@Param("examId") Long examId);

    @Delete("delete from exam_draft_node_attachments where draft_node_id in (select id from exam_draft_nodes where exam_id = #{examId})")
    void deleteDraftNodeAttachments(@Param("examId") Long examId);

    @Delete("delete from exam_draft_node_options where draft_node_id in (select id from exam_draft_nodes where exam_id = #{examId})")
    void deleteDraftNodeOptions(@Param("examId") Long examId);

    @Delete("delete from exam_draft_nodes where exam_id = #{examId} and parent_id is not null")
    void deleteDraftChildNodes(@Param("examId") Long examId);

    @Delete("delete from exam_draft_nodes where exam_id = #{examId} and parent_id is null")
    void deleteDraftRootNodes(@Param("examId") Long examId);

    @Insert("""
            insert into exam_draft_questions (
              exam_id, draft_node_id, source_question_id, bank_id, bank_name, type, stem,
              item_label, item_stem, analysis, score, sort_order
            )
            values (
              #{examId}, #{draftNodeId}, #{sourceQuestionId}, #{bankId}, #{bankName}, #{type}, #{stem},
              #{itemLabel}, #{itemStem}, #{analysis}, #{score}, #{sortOrder}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDraftQuestion(Map<String, Object> question);

    @Insert("""
            insert into exam_draft_options (draft_question_id, option_label, content, is_correct, sort_order)
            select #{draftQuestionId}, option_label, content, is_correct, sort_order
            from question_options
            where question_id = #{sourceQuestionId}
            order by sort_order, id
            """)
    void copyDraftOptionsFromSource(@Param("draftQuestionId") Long draftQuestionId, @Param("sourceQuestionId") Long sourceQuestionId);

    @Insert("""
            insert into exam_draft_answer_labels (draft_question_id, answer_label, sort_order)
            select #{draftQuestionId}, answer_label, sort_order
            from question_answer_labels
            where question_id = #{sourceQuestionId}
            order by sort_order, id
            """)
    void copyDraftAnswerLabelsFromSource(@Param("draftQuestionId") Long draftQuestionId, @Param("sourceQuestionId") Long sourceQuestionId);

    @Insert("""
            insert into exam_draft_attachments (draft_question_id, file_name, file_url, media_type, sort_order)
            select #{draftQuestionId}, file_name, file_url, media_type, sort_order
            from question_attachments
            where question_id = #{sourceQuestionId}
            order by sort_order, id
            """)
    void copyDraftAttachmentsFromSource(@Param("draftQuestionId") Long draftQuestionId, @Param("sourceQuestionId") Long sourceQuestionId);

    @Select("""
            select edq.id,
                   edq.exam_id as examId,
                   edq.draft_node_id as draftNodeId,
                   group_node.source_node_id as sourceNodeId,
                   edq.source_question_id as questionId,
                   edq.type,
                   edq.score,
                   edq.sort_order as sortOrder,
                   edq.bank_id as bankId,
                   edq.bank_name as bankName,
                   edq.stem,
            """ + DRAFT_NODE_FIELDS + """
                   ,
                   edq.item_label as itemLabel,
                   edq.item_stem as itemStem,
                   edq.analysis,
                   q.status
            from exam_draft_questions edq
            left join questions q on q.id = edq.source_question_id
            left join exam_draft_nodes group_node on group_node.id = edq.draft_node_id
            left join exam_draft_nodes section_node on section_node.id = group_node.parent_id
            where edq.exam_id = #{examId}
            order by edq.sort_order, edq.id
            """)
    List<Map<String, Object>> findDraftQuestions(@Param("examId") Long examId);

    @Select("""
            select q.id as questionId,
                   q.bank_id as bankId,
                   qb.name as bankName,
                   q.node_id as sourceNodeId,
                   q.type,
                   q.stem,
            """ + SOURCE_NODE_FIELDS + """
                   ,
                   q.item_label as itemLabel,
                   q.item_stem as itemStem,
                   q.analysis,
                   q.status
            from questions q
            join question_banks qb on qb.id = q.bank_id
            left join question_nodes group_node on group_node.id = q.node_id
            left join question_nodes section_node on section_node.id = group_node.parent_id
            where q.id = #{questionId}
            """)
    Map<String, Object> findSourceQuestion(@Param("questionId") Long questionId);

    @Select("""
            select id, option_label as label, content, is_correct as correct, sort_order as sortOrder
            from question_options
            where question_id = #{questionId}
            union all
            select qno.id,
                   qno.option_label as label,
                   qno.content,
                   exists (
                     select 1 from question_answer_labels qal
                     where qal.question_id = q.id and qal.answer_label = qno.option_label
                   ) as correct,
                   qno.sort_order as sortOrder
            from questions q
            join question_node_options qno on qno.node_id = q.node_id
            where q.id = #{questionId}
              and not exists (select 1 from question_options qo where qo.question_id = q.id)
            order by sortOrder, id
            """)
    List<Map<String, Object>> findSourceOptions(@Param("questionId") Long questionId);

    @Select("""
            select id, option_label as label, content, is_correct as correct, sort_order as sortOrder
            from exam_draft_options
            where draft_question_id = #{draftQuestionId}
            union all
            select dno.id,
                   dno.option_label as label,
                   dno.content,
                   exists (
                     select 1 from exam_draft_answer_labels dal
                     where dal.draft_question_id = edq.id and dal.answer_label = dno.option_label
                   ) as correct,
                   dno.sort_order as sortOrder
            from exam_draft_questions edq
            join exam_draft_node_options dno on dno.draft_node_id = edq.draft_node_id
            where edq.id = #{draftQuestionId}
              and not exists (select 1 from exam_draft_options edo where edo.draft_question_id = edq.id)
            order by sortOrder, id
            """)
    List<Map<String, Object>> findDraftOptions(@Param("draftQuestionId") Long draftQuestionId);

    @Select("""
            select id, file_name as fileName, file_url as fileUrl, media_type as mediaType, sort_order as sortOrder
            from exam_draft_attachments
            where draft_question_id = #{draftQuestionId}
            union all
            select dna.id, dna.file_name as fileName, dna.file_url as fileUrl, dna.media_type as mediaType, dna.sort_order as sortOrder
            from exam_draft_questions edq
            join exam_draft_node_attachments dna on dna.draft_node_id = edq.draft_node_id
            where edq.id = #{draftQuestionId}
            union all
            select sna.id, sna.file_name as fileName, sna.file_url as fileUrl, sna.media_type as mediaType, sna.sort_order as sortOrder
            from exam_draft_questions edq
            join exam_draft_nodes group_node on group_node.id = edq.draft_node_id
            join exam_draft_node_attachments sna on sna.draft_node_id = group_node.parent_id
            where edq.id = #{draftQuestionId}
            order by sortOrder, id
            """)
    List<Map<String, Object>> findDraftAttachments(@Param("draftQuestionId") Long draftQuestionId);

    @Select("select * from exam_draft_nodes where id = #{draftNodeId}")
    Map<String, Object> findDraftNode(@Param("draftNodeId") Long draftNodeId);

    @Select("""
            select *
            from exam_published_nodes
            where exam_id = #{examId}
              and source_node_id = #{sourceNodeId}
            """)
    Map<String, Object> findPublishedNodeBySource(@Param("examId") Long examId, @Param("sourceNodeId") Long sourceNodeId);

    @Insert("""
            insert into exam_published_nodes (exam_id, source_node_id, parent_id, node_code, node_type, title, direction, material, sort_order)
            values (#{examId}, #{sourceNodeId}, #{parentId}, #{nodeCode}, #{nodeType}, #{title}, #{direction}, #{material}, #{sortOrder})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPublishedNode(Map<String, Object> node);

    @Insert("""
            insert into exam_published_node_options (published_node_id, option_label, content, sort_order)
            select #{publishedNodeId}, option_label, content, sort_order
            from exam_draft_node_options
            where draft_node_id = #{draftNodeId}
            order by sort_order, id
            """)
    void copyPublishedNodeOptions(@Param("publishedNodeId") Long publishedNodeId, @Param("draftNodeId") Long draftNodeId);

    @Insert("""
            insert into exam_published_node_attachments (published_node_id, file_name, file_url, media_type, sort_order)
            select #{publishedNodeId}, file_name, file_url, media_type, sort_order
            from exam_draft_node_attachments
            where draft_node_id = #{draftNodeId}
            order by sort_order, id
            """)
    void copyPublishedNodeAttachments(@Param("publishedNodeId") Long publishedNodeId, @Param("draftNodeId") Long draftNodeId);

    @Delete("delete from exam_published_attachments where published_question_id in (select id from exam_published_questions where exam_id = #{examId})")
    void deletePublishedAttachments(@Param("examId") Long examId);

    @Delete("delete from exam_published_options where published_question_id in (select id from exam_published_questions where exam_id = #{examId})")
    void deletePublishedOptions(@Param("examId") Long examId);

    @Delete("delete from exam_published_answer_labels where published_question_id in (select id from exam_published_questions where exam_id = #{examId})")
    void deletePublishedAnswerLabels(@Param("examId") Long examId);

    @Delete("delete from exam_published_questions where exam_id = #{examId}")
    void deletePublishedQuestions(@Param("examId") Long examId);

    @Delete("delete from exam_published_node_attachments where published_node_id in (select id from exam_published_nodes where exam_id = #{examId})")
    void deletePublishedNodeAttachments(@Param("examId") Long examId);

    @Delete("delete from exam_published_node_options where published_node_id in (select id from exam_published_nodes where exam_id = #{examId})")
    void deletePublishedNodeOptions(@Param("examId") Long examId);

    @Delete("delete from exam_published_nodes where exam_id = #{examId} and parent_id is not null")
    void deletePublishedChildNodes(@Param("examId") Long examId);

    @Delete("delete from exam_published_nodes where exam_id = #{examId} and parent_id is null")
    void deletePublishedRootNodes(@Param("examId") Long examId);

    @Delete("delete from exam_departments where exam_id = #{examId}")
    void deleteAllExamDepartments(@Param("examId") Long examId);

    @Insert("""
            insert into exam_published_questions (
              exam_id, published_node_id, source_question_id, bank_id, bank_name, type, stem,
              item_label, item_stem, analysis, score, sort_order
            )
            values (
              #{examId}, #{publishedNodeId}, #{sourceQuestionId}, #{bankId}, #{bankName}, #{type}, #{stem},
              #{itemLabel}, #{itemStem}, #{analysis}, #{score}, #{sortOrder}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPublishedQuestion(Map<String, Object> question);

    @Select("""
            select edq.id as draftQuestionId,
                   edq.draft_node_id as draftNodeId,
                   group_node.source_node_id as sourceNodeId,
                   edq.source_question_id as questionId,
                   edq.bank_id as bankId,
                   edq.bank_name as bankName,
                   edq.type,
                   edq.stem,
            """ + DRAFT_NODE_FIELDS + """
                   ,
                   edq.item_label as itemLabel,
                   edq.item_stem as itemStem,
                   edq.analysis,
                   edq.score,
                   edq.sort_order as sortOrder,
                   q.status
            from exam_draft_questions edq
            left join questions q on q.id = edq.source_question_id
            left join exam_draft_nodes group_node on group_node.id = edq.draft_node_id
            left join exam_draft_nodes section_node on section_node.id = group_node.parent_id
            where edq.exam_id = #{examId}
              and q.status = 'ACTIVE'
            order by edq.sort_order, edq.id
            """)
    List<Map<String, Object>> findDraftQuestionsForPublish(@Param("examId") Long examId);

    @Insert("""
            insert into exam_published_options (published_question_id, option_label, content, is_correct, sort_order)
            select #{publishedQuestionId}, option_label, content, is_correct, sort_order
            from exam_draft_options
            where draft_question_id = #{draftQuestionId}
            order by sort_order, id
            """)
    void copyPublishedOptions(@Param("publishedQuestionId") Long publishedQuestionId, @Param("draftQuestionId") Long draftQuestionId);

    @Insert("""
            insert into exam_published_answer_labels (published_question_id, answer_label, sort_order)
            select #{publishedQuestionId}, answer_label, sort_order
            from exam_draft_answer_labels
            where draft_question_id = #{draftQuestionId}
            order by sort_order, id
            """)
    void copyPublishedAnswerLabels(@Param("publishedQuestionId") Long publishedQuestionId, @Param("draftQuestionId") Long draftQuestionId);

    @Insert("""
            insert into exam_published_attachments (published_question_id, file_name, file_url, media_type, sort_order)
            select #{publishedQuestionId}, file_name, file_url, media_type, sort_order
            from exam_draft_attachments
            where draft_question_id = #{draftQuestionId}
            order by sort_order, id
            """)
    void copyPublishedAttachments(@Param("publishedQuestionId") Long publishedQuestionId, @Param("draftQuestionId") Long draftQuestionId);

    @Select("select coalesce(sum(score), 0) from exam_published_questions where exam_id = #{examId}")
    BigDecimal findPublishedTotalScore(@Param("examId") Long examId);

    @Select("select count(*) from exam_published_questions where exam_id = #{examId}")
    int countPublishedQuestions(@Param("examId") Long examId);

    @Select("""
            select epq.id,
                   epq.exam_id as examId,
                   epq.published_node_id as publishedNodeId,
                   group_node.source_node_id as sourceNodeId,
                   epq.source_question_id as sourceQuestionId,
                   epq.bank_id as bankId,
                   epq.bank_name as bankName,
                   epq.type,
                   epq.stem,
            """ + PUBLISHED_NODE_FIELDS + """
                   ,
                   epq.item_label as itemLabel,
                   epq.item_stem as itemStem,
                   epq.analysis,
                   epq.score,
                   epq.sort_order as sortOrder
            from exam_published_questions epq
            left join exam_published_nodes group_node on group_node.id = epq.published_node_id
            left join exam_published_nodes section_node on section_node.id = group_node.parent_id
            where epq.exam_id = #{examId}
            order by epq.sort_order, epq.id
            """)
    List<Map<String, Object>> findPublishedQuestions(@Param("examId") Long examId);

    @Select("""
            select id, option_label as label, content, is_correct as correct, sort_order as sortOrder
            from exam_published_options
            where published_question_id = #{publishedQuestionId}
            union all
            select pno.id,
                   pno.option_label as label,
                   pno.content,
                   exists (
                     select 1 from exam_published_answer_labels pal
                     where pal.published_question_id = epq.id and pal.answer_label = pno.option_label
                   ) as correct,
                   pno.sort_order as sortOrder
            from exam_published_questions epq
            join exam_published_node_options pno on pno.published_node_id = epq.published_node_id
            where epq.id = #{publishedQuestionId}
              and not exists (select 1 from exam_published_options epo where epo.published_question_id = epq.id)
            order by sortOrder, id
            """)
    List<Map<String, Object>> findPublishedOptions(@Param("publishedQuestionId") Long publishedQuestionId);

    @Select("""
            select id, file_name as fileName, file_url as fileUrl, media_type as mediaType, sort_order as sortOrder
            from exam_published_attachments
            where published_question_id = #{publishedQuestionId}
            union all
            select pna.id, pna.file_name as fileName, pna.file_url as fileUrl, pna.media_type as mediaType, pna.sort_order as sortOrder
            from exam_published_questions epq
            join exam_published_node_attachments pna on pna.published_node_id = epq.published_node_id
            where epq.id = #{publishedQuestionId}
            union all
            select sna.id, sna.file_name as fileName, sna.file_url as fileUrl, sna.media_type as mediaType, sna.sort_order as sortOrder
            from exam_published_questions epq
            join exam_published_nodes group_node on group_node.id = epq.published_node_id
            join exam_published_node_attachments sna on sna.published_node_id = group_node.parent_id
            where epq.id = #{publishedQuestionId}
            order by sortOrder, id
            """)
    List<Map<String, Object>> findPublishedAttachments(@Param("publishedQuestionId") Long publishedQuestionId);
}
