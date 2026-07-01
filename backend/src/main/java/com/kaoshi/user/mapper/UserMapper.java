package com.kaoshi.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaoshi.user.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<UserAccount> {
    @Select("select * from users where username = #{username} and deleted_at is null")
    Optional<UserAccount> findByUsername(@Param("username") String username);

    @Select("""
            select r.code
            from roles r
            join user_roles ur on ur.role_id = r.id
            where ur.user_id = #{userId}
            order by r.code
            """)
    List<String> findRoleCodes(@Param("userId") Long userId);

    @Select("""
            select distinct p.code
            from permissions p
            join role_permissions rp on rp.permission_id = p.id
            join user_roles ur on ur.role_id = rp.role_id
            where ur.user_id = #{userId}
            order by p.code
            """)
    List<String> findPermissionCodes(@Param("userId") Long userId);
}

