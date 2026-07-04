package com.kaoshi.question.mapper;

import com.kaoshi.question.domain.Question;
import com.kaoshi.question.domain.QuestionNode;
import com.kaoshi.question.domain.QuestionNodeAttachment;
import com.kaoshi.question.domain.QuestionNodeOption;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface QuestionContentMapper {
    String QUESTION_WITH_NODE_FIELDS = QuestionMapper.QUESTION_WITH_NODE_FIELDS;
    String QUESTION_NODE_JOINS = QuestionMapper.QUESTION_NODE_JOINS;

    @Select("select name from question_banks where id = #{bankId}")
    String findBankName(@Param("bankId") Long bankId);

    @Select("select count(*) from question_banks where id = #{bankId}")
    int countBankById(@Param("bankId") Long bankId);

    @Select("""
            select id, bank_id as bankId, parent_id as parentId, node_code as nodeCode, node_type as nodeType,
                   title, direction, material, sort_order as sortOrder
            from question_nodes
            where bank_id = #{bankId}
            order by case when parent_id is null then 0 else 1 end, sort_order, id
            """)
    List<QuestionNode> findNodesByBank(@Param("bankId") Long bankId);

    @Select("""
            select id, bank_id as bankId, parent_id as parentId, node_code as nodeCode, node_type as nodeType,
                   title, direction, material, sort_order as sortOrder
            from question_nodes
            where id = #{id}
            """)
    QuestionNode findNodeById(@Param("id") Long id);

    @Select("select count(*) from question_nodes where bank_id = #{bankId} and node_code = #{nodeCode} and id <> #{id}")
    int countNodeCodeExceptId(@Param("bankId") Long bankId, @Param("nodeCode") String nodeCode, @Param("id") Long id);

    @Select("select count(*) from question_nodes where bank_id = #{bankId} and node_code = #{nodeCode}")
    int countNodeCode(@Param("bankId") Long bankId, @Param("nodeCode") String nodeCode);

    @Insert("""
            insert into question_nodes (bank_id, parent_id, node_code, node_type, title, direction, material, sort_order)
            values (#{bankId}, #{parentId}, #{nodeCode}, #{nodeType}, #{title}, #{direction}, #{material}, #{sortOrder})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertNode(QuestionNode node);

    @Update("""
            update question_nodes
            set parent_id = #{parentId},
                node_code = #{nodeCode},
                node_type = #{nodeType},
                title = #{title},
                direction = #{direction},
                material = #{material},
                sort_order = #{sortOrder},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateNode(QuestionNode node);

    @Select("select count(*) from question_nodes where parent_id = #{nodeId}")
    int countChildNodes(@Param("nodeId") Long nodeId);

    @Select("select count(*) from questions where node_id = #{nodeId}")
    int countQuestionsByNode(@Param("nodeId") Long nodeId);

    @Delete("delete from question_node_attachments where node_id = #{nodeId}")
    void deleteNodeAttachments(@Param("nodeId") Long nodeId);

    @Delete("delete from question_node_options where node_id = #{nodeId}")
    void deleteNodeOptions(@Param("nodeId") Long nodeId);

    @Delete("delete from question_nodes where id = #{nodeId}")
    int deleteNode(@Param("nodeId") Long nodeId);

    @Select("""
            select id, node_id, option_label, content, sort_order
            from question_node_options
            where node_id = #{nodeId}
            order by sort_order, id
            """)
    List<QuestionNodeOption> findNodeOptions(@Param("nodeId") Long nodeId);

    @Insert("""
            insert into question_node_options (node_id, option_label, content, sort_order)
            values (#{nodeId}, #{label}, #{content}, #{sortOrder})
            """)
    void insertNodeOption(
            @Param("nodeId") Long nodeId,
            @Param("label") String label,
            @Param("content") String content,
            @Param("sortOrder") int sortOrder
    );

    @Select("""
            select id, node_id, file_name, file_url, media_type, sort_order
            from question_node_attachments
            where node_id = #{nodeId}
            order by sort_order, id
            """)
    List<QuestionNodeAttachment> findNodeAttachments(@Param("nodeId") Long nodeId);

    @Insert("""
            insert into question_node_attachments (node_id, file_name, file_url, media_type, sort_order)
            values (#{nodeId}, #{fileName}, #{fileUrl}, #{mediaType}, #{sortOrder})
            """)
    void insertNodeAttachment(
            @Param("nodeId") Long nodeId,
            @Param("fileName") String fileName,
            @Param("fileUrl") String fileUrl,
            @Param("mediaType") String mediaType,
            @Param("sortOrder") int sortOrder
    );

    @Select("""
            select
            """ + QUESTION_WITH_NODE_FIELDS + """
            from questions q
            """ + QUESTION_NODE_JOINS + """
            where q.bank_id = #{bankId}
              and q.node_id is null
            order by q.id
            """)
    List<Question> findUngroupedQuestions(@Param("bankId") Long bankId);

    @Select("""
            select
            """ + QUESTION_WITH_NODE_FIELDS + """
            from questions q
            """ + QUESTION_NODE_JOINS + """
            where q.node_id = #{nodeId}
            order by q.id
            """)
    List<Question> findQuestionsByNode(@Param("nodeId") Long nodeId);
}
