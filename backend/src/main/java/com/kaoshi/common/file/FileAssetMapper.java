package com.kaoshi.common.file;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FileAssetMapper {
    @Insert("""
            insert into file_assets (original_name, file_url, media_type, usage_type, uploaded_by)
            values (#{originalName}, #{fileUrl}, #{mediaType}, #{usageType}, #{uploadedBy})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Map<String, Object> asset);

    @Select("""
            select fa.id,
                   fa.original_name as originalName,
                   fa.file_url as fileUrl,
                   fa.media_type as mediaType,
                   fa.usage_type as usageType,
                   u.username as uploadedBy,
                   fa.uploaded_at as uploadedAt
            from file_assets fa
            left join users u on u.id = fa.uploaded_by
            order by fa.id desc
            limit #{limit}
            """)
    List<Map<String, Object>> latest(@Param("limit") int limit);
}
