package com.kaoshi.question.mapper;

import com.kaoshi.question.domain.Question;
import com.kaoshi.question.domain.QuestionAttachment;
import com.kaoshi.question.domain.QuestionOption;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionMapper {
    String QUESTION_WITH_NODE_FIELDS = """
            q.id,
            q.bank_id,
            q.node_id,
            q.type,
            q.stem,
            case when section_node.id is null then null else section_node.node_code end as section_code,
            case when section_node.id is null then null else section_node.title end as section_title,
            case when section_node.id is null then 0 else section_node.sort_order end as section_sort_order,
            case when group_node.id is null then null else group_node.node_code end as group_code,
            case when group_node.id is null then null else group_node.title end as group_title,
            case when group_node.id is null then null else group_node.direction end as group_direction,
            case when group_node.id is null then null else group_node.material end as group_material,
            case when group_node.id is null then 0 else group_node.sort_order end as group_sort_order,
            q.item_label,
            q.item_stem,
            q.analysis,
            q.difficulty,
            q.status
            """;

    String QUESTION_NODE_JOINS = """
            left join question_nodes group_node on group_node.id = q.node_id
            left join question_nodes section_node on section_node.id = group_node.parent_id
            """;

    @Select("""
            select count(*)
            from questions q
            join question_banks b on b.id = q.bank_id
            where (#{bankId} is null or q.bank_id = #{bankId})
              and (#{keyword} is null or q.stem like #{keyword} or b.name like #{keyword})
            """)
    long countQuestions(@Param("bankId") Long bankId, @Param("keyword") String keyword);

    @Select("""
            select
            """ + QUESTION_WITH_NODE_FIELDS + """
            from questions q
            join question_banks b on b.id = q.bank_id
            """ + QUESTION_NODE_JOINS + """
            where (#{bankId} is null or q.bank_id = #{bankId})
              and (#{keyword} is null or q.stem like #{keyword} or b.name like #{keyword})
            order by q.id desc
            limit #{size} offset #{offset}
            """)
    List<Question> findQuestions(
            @Param("bankId") Long bankId,
            @Param("keyword") String keyword,
            @Param("size") int size,
            @Param("offset") int offset
    );

    @Select("""
            select
            """ + QUESTION_WITH_NODE_FIELDS + """
            from questions q
            """ + QUESTION_NODE_JOINS + """
            where q.id = #{id}
            """)
    Question findQuestionById(@Param("id") Long id);

    @Select("select name from question_banks where id = #{id}")
    String findBankName(@Param("id") Long id);

    @Select("select name from question_banks where status = 'ACTIVE' order by id")
    List<String> findActiveBankNames();

    @Select("select id from question_banks where name = #{name}")
    Long findBankIdByName(@Param("name") String name);

    @Select("select count(*) from question_banks where id = #{id}")
    long countBankById(@Param("id") Long id);

    @Select("""
            select id
            from question_nodes
            where bank_id = #{bankId}
              and node_code = #{nodeCode}
              and node_type = #{nodeType}
            limit 1
            """)
    Long findNodeId(
            @Param("bankId") Long bankId,
            @Param("nodeCode") String nodeCode,
            @Param("nodeType") String nodeType
    );

    @Insert("""
            insert into question_nodes (bank_id, parent_id, node_code, node_type, title, direction, material, sort_order)
            values (#{bankId}, #{parentId}, #{nodeCode}, #{nodeType}, #{title}, #{direction}, #{material}, #{sortOrder})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertNode(Map<String, Object> node);

    @Update("""
            update question_nodes
            set parent_id = #{parentId},
                title = #{title},
                direction = #{direction},
                material = #{material},
                sort_order = #{sortOrder},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateNode(Map<String, Object> node);

    @Insert("""
            insert into questions (
              bank_id, node_id, type, stem, item_label, item_stem, analysis, difficulty, status
            )
            values (
              #{bankId}, #{nodeId}, #{type}, #{stem}, #{itemLabel}, #{itemStem}, #{analysis}, #{difficulty}, #{status}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertQuestion(Question question);

    @Update("""
            update questions
            set bank_id = #{bankId},
                node_id = #{nodeId},
                type = #{type},
                stem = #{stem},
                item_label = #{itemLabel},
                item_stem = #{itemStem},
                analysis = #{analysis},
                difficulty = #{difficulty},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateQuestion(Question question);

    @Select("""
            select id, question_id, option_label, content, is_correct as correct, sort_order
            from question_options
            where question_id = #{questionId}
            union all
            select qno.id,
                   q.id as question_id,
                   qno.option_label,
                   qno.content,
                   exists (
                     select 1
                     from question_answer_labels qal
                     where qal.question_id = q.id
                       and qal.answer_label = qno.option_label
                   ) as correct,
                   qno.sort_order
            from questions q
            join question_node_options qno on qno.node_id = q.node_id
            where q.id = #{questionId}
              and not exists (
                select 1 from question_options qo where qo.question_id = q.id
              )
            order by sort_order, id
            """)
    List<QuestionOption> findOptions(@Param("questionId") Long questionId);

    @Insert("""
            insert into question_options (question_id, option_label, content, is_correct, sort_order)
            values (#{questionId}, #{optionLabel}, #{content}, #{correct}, #{sortOrder})
            """)
    void insertOption(QuestionOption option);

    @Delete("delete from question_options where question_id = #{questionId}")
    void deleteOptions(@Param("questionId") Long questionId);

    @Delete("delete from question_answer_labels where question_id = #{questionId}")
    void deleteAnswerLabels(@Param("questionId") Long questionId);

    @Insert("""
            insert into question_answer_labels (question_id, answer_label, sort_order)
            values (#{questionId}, #{answerLabel}, #{sortOrder})
            """)
    void insertAnswerLabel(
            @Param("questionId") Long questionId,
            @Param("answerLabel") String answerLabel,
            @Param("sortOrder") int sortOrder
    );

    @Select("select * from question_attachments where question_id = #{questionId} order by sort_order, id")
    List<QuestionAttachment> findAttachments(@Param("questionId") Long questionId);

    @Insert("""
            insert into question_attachments (question_id, file_name, file_url, media_type, sort_order)
            values (#{questionId}, #{fileName}, #{fileUrl}, #{mediaType}, #{sortOrder})
            """)
    void insertAttachment(
            @Param("questionId") Long questionId,
            @Param("fileName") String fileName,
            @Param("fileUrl") String fileUrl,
            @Param("mediaType") String mediaType,
            @Param("sortOrder") int sortOrder
    );

    @Delete("delete from question_attachments where question_id = #{questionId}")
    void deleteAttachments(@Param("questionId") Long questionId);
}
