package com.kaoshi.admin.user.mapper;

import com.kaoshi.user.domain.UserAccount;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper {
    @Select("""
            select count(*)
            from users
            where deleted_at is null
              and (#{keyword} is null or username like #{keyword} or display_name like #{keyword})
            """)
    long countUsers(@Param("keyword") String keyword);

    @Select("""
            select *
            from users
            where deleted_at is null
              and (#{keyword} is null or username like #{keyword} or display_name like #{keyword})
            order by id desc
            limit #{size} offset #{offset}
            """)
    List<UserAccount> findUsers(@Param("keyword") String keyword, @Param("size") int size, @Param("offset") int offset);

    @Select("select count(*) from users where username = #{username} and deleted_at is null")
    int countByUsername(@Param("username") String username);

    @Select("select count(*) from departments where id = #{departmentId} and status = 'ACTIVE'")
    int countActiveDepartmentById(@Param("departmentId") Long departmentId);

    @Select("select name from departments where id = #{departmentId}")
    String findDepartmentName(@Param("departmentId") Long departmentId);

    @Insert("""
            insert into user_roles (user_id, role_id)
            values (#{userId}, #{roleId})
            """)
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("delete from user_roles where user_id = #{userId}")
    void deleteUserRoles(@Param("userId") Long userId);

    @Select("""
            select r.code
            from roles r
            join user_roles ur on ur.role_id = r.id
            where ur.user_id = #{userId}
            order by r.code
            """)
    List<String> findRoleCodes(@Param("userId") Long userId);
}

