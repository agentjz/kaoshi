package com.kaoshi.admin.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaoshi.admin.menu.domain.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface AdminMenuMapper extends BaseMapper<Menu> {
    @Select("select * from menus order by sort_order, id")
    List<Menu> findAll();

    @Select("""
            <script>
            select count(*)
            from menus
            where id in
            <foreach collection="ids" item="id" open="(" separator="," close=")">
              #{id}
            </foreach>
            </script>
            """)
    long countByIds(@Param("ids") Set<Long> ids);
}

