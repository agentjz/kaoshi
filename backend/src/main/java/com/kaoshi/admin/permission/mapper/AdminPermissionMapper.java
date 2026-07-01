package com.kaoshi.admin.permission.mapper;

import com.kaoshi.user.domain.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface AdminPermissionMapper {
    @Select("select * from permissions order by id")
    List<Permission> findAll();

    @Select("""
            <script>
            select count(*)
            from permissions
            where id in
            <foreach collection="ids" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    long countByIds(@Param("ids") Set<Long> ids);
}

