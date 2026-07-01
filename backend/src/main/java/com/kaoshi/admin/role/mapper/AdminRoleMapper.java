package com.kaoshi.admin.role.mapper;

import com.kaoshi.admin.menu.domain.Menu;
import com.kaoshi.user.domain.Permission;
import com.kaoshi.user.domain.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

@Mapper
public interface AdminRoleMapper {
    @Select("select * from roles order by id")
    List<Role> findAll();

    @Select("select * from roles where id = #{id}")
    Role findById(@Param("id") Long id);

    @Select("select count(*) from roles where code = #{code}")
    int countByCode(@Param("code") String code);

    @Select("select count(*) from roles where code = #{code} and id <> #{id}")
    int countByCodeExceptId(@Param("code") String code, @Param("id") Long id);

    @Select("""
            <script>
            select count(*)
            from roles
            where id in
            <foreach collection="ids" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    long countByIds(@Param("ids") Set<Long> ids);

    @Insert("""
            insert into roles (code, name, description)
            values (#{code}, #{name}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRole(Role role);

    @Update("""
            update roles
            set code = #{code},
                name = #{name},
                description = #{description},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateRole(Role role);

    @Insert("insert into role_permissions (role_id, permission_id) values (#{roleId}, #{permissionId})")
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Delete("delete from role_permissions where role_id = #{roleId}")
    void deleteRolePermissions(@Param("roleId") Long roleId);

    @Insert("insert into role_menus (role_id, menu_id) values (#{roleId}, #{menuId})")
    void insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Delete("delete from role_menus where role_id = #{roleId}")
    void deleteRoleMenus(@Param("roleId") Long roleId);

    @Select("""
            select p.*
            from permissions p
            join role_permissions rp on rp.permission_id = p.id
            where rp.role_id = #{roleId}
            order by p.id
            """)
    List<Permission> findPermissions(@Param("roleId") Long roleId);

    @Select("""
            select m.*
            from menus m
            join role_menus rm on rm.menu_id = m.id
            where rm.role_id = #{roleId}
            order by m.sort_order, m.id
            """)
    List<Menu> findMenus(@Param("roleId") Long roleId);
}
