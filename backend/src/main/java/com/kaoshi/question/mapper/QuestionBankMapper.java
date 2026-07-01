package com.kaoshi.question.mapper;

import com.kaoshi.question.domain.QuestionBank;
import com.kaoshi.question.domain.QuestionCategory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface QuestionBankMapper {
    @Select("select * from question_categories order by sort_order, id")
    List<QuestionCategory> findCategories();

    @Select("select count(*) from question_categories where id = #{id}")
    long countCategoryById(@Param("id") Long id);

    @Select("""
            select count(*)
            from question_banks b
            join question_categories c on c.id = b.category_id
            where #{keyword} is null or b.name like #{keyword} or c.name like #{keyword}
            """)
    long countBanks(@Param("keyword") String keyword);

    @Select("""
            select b.*
            from question_banks b
            join question_categories c on c.id = b.category_id
            where #{keyword} is null or b.name like #{keyword} or c.name like #{keyword}
            order by b.id desc
            limit #{size} offset #{offset}
            """)
    List<QuestionBank> findBanks(@Param("keyword") String keyword, @Param("size") int size, @Param("offset") int offset);

    @Select("select * from question_banks where id = #{id}")
    QuestionBank findBankById(@Param("id") Long id);

    @Select("select name from question_categories where id = #{id}")
    String findCategoryName(@Param("id") Long id);

    @Insert("""
            insert into question_banks (category_id, name, description, status)
            values (#{categoryId}, #{name}, #{description}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBank(QuestionBank bank);

    @Update("""
            update question_banks
            set category_id = #{categoryId},
                name = #{name},
                description = #{description},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateBank(QuestionBank bank);
}

