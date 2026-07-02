package com.kaoshi.system.department.mapper;

import com.kaoshi.system.department.domain.Department;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    @Select("select * from departments order by parent_id, id")
    List<Department> findAll();

    @Select("select * from departments where id = #{id}")
    Department findById(@Param("id") Long id);

    @Select("select * from departments where code = #{code}")
    Department findByCode(@Param("code") String code);

    @Select("select count(*) from departments where code = #{code}")
    int countByCode(@Param("code") String code);

    @Select("select count(*) from departments where code = #{code} and id <> #{id}")
    int countByCodeExceptId(@Param("code") String code, @Param("id") Long id);

    @Select("select count(*) from departments where parent_id = #{parentId}")
    int countChildren(@Param("parentId") Long parentId);

    @Insert("""
            insert into departments (parent_id, name, code, description, status)
            values (#{parentId}, #{name}, #{code}, #{description}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDepartment(Department department);

    @Update("""
            update departments
            set parent_id = #{parentId},
                name = #{name},
                code = #{code},
                description = #{description},
                status = #{status},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateDepartment(Department department);

    @Delete("delete from departments where id = #{id}")
    int deleteDepartment(@Param("id") Long id);
}
