package com.kaoshi.paper.mapper;

import com.kaoshi.paper.domain.Paper;
import com.kaoshi.paper.domain.PaperCategory;
import com.kaoshi.paper.domain.PaperQuestion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PaperMapper {
    @Select("select * from paper_categories order by sort_order, id")
    List<PaperCategory> findCategories();

    @Select("select count(*) from paper_categories where id = #{id}")
    long countCategoryById(@Param("id") Long id);

    @Select("select name from paper_categories where id = #{id}")
    String findCategoryName(@Param("id") Long id);

    @Select("""
            select count(*)
            from papers p
            join paper_categories c on c.id = p.category_id
            where #{keyword} is null or p.name like #{keyword} or c.name like #{keyword}
            """)
    long countPapers(@Param("keyword") String keyword);

    @Select("""
            select p.*
            from papers p
            join paper_categories c on c.id = p.category_id
            where #{keyword} is null or p.name like #{keyword} or c.name like #{keyword}
            order by p.id desc
            limit #{size} offset #{offset}
            """)
    List<Paper> findPapers(@Param("keyword") String keyword, @Param("size") int size, @Param("offset") int offset);

    @Select("select * from papers where id = #{id}")
    Paper findPaperById(@Param("id") Long id);

    @Insert("""
            insert into papers (category_id, name, description, total_score, duration_minutes, status)
            values (#{categoryId}, #{name}, #{description}, #{totalScore}, #{durationMinutes}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertPaper(Paper paper);

    @Update("""
            update papers
            set category_id = #{categoryId},
                name = #{name},
                description = #{description},
                total_score = #{totalScore},
                duration_minutes = #{durationMinutes},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updatePaper(Paper paper);

    @Select("""
            select pq.*
            from paper_questions pq
            where pq.paper_id = #{paperId}
            order by pq.sort_order, pq.id
            """)
    List<PaperQuestion> findPaperQuestions(@Param("paperId") Long paperId);

    @Select("select count(*) from questions where id = #{questionId} and status = 'ACTIVE'")
    long countActiveQuestionById(@Param("questionId") Long questionId);

    @Select("select type from questions where id = #{questionId}")
    String findQuestionType(@Param("questionId") Long questionId);

    @Select("select stem from questions where id = #{questionId}")
    String findQuestionStem(@Param("questionId") Long questionId);

    @Insert("""
            insert into paper_questions (paper_id, question_id, score, sort_order)
            values (#{paperId}, #{questionId}, #{score}, #{sortOrder})
            """)
    void insertPaperQuestion(PaperQuestion paperQuestion);

    @Delete("delete from paper_questions where paper_id = #{paperId}")
    void deletePaperQuestions(@Param("paperId") Long paperId);
}

