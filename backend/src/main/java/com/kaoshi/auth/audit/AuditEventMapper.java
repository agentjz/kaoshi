package com.kaoshi.auth.audit;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditEventMapper {
    @Insert("""
            insert into audit_events (
              actor_user_id, actor_username, action, resource_type, resource_id, resource_title,
              ip_address, user_agent, payload_json
            ) values (
              #{actorUserId}, #{actorUsername}, #{action}, #{resourceType}, #{resourceId}, #{resourceTitle},
              #{ipAddress}, #{userAgent}, #{payloadJson}
            )
            """)
    void insert(
            @Param("actorUserId") Long actorUserId,
            @Param("actorUsername") String actorUsername,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId,
            @Param("resourceTitle") String resourceTitle,
            @Param("ipAddress") String ipAddress,
            @Param("userAgent") String userAgent,
            @Param("payloadJson") String payloadJson
    );
}
