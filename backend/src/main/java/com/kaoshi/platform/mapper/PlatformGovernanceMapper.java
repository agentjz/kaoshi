package com.kaoshi.platform.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface PlatformGovernanceMapper {
    @Select("""
            select id,
                   recipient_user_id as recipientUserId,
                   title,
                   content,
                   category,
                   read_at as readAt,
                   created_at as createdAt
            from notifications
            where recipient_user_id is null or recipient_user_id = #{userId}
            order by id desc
            limit 100
            """)
    List<Map<String, Object>> findNotifications(@Param("userId") Long userId);

    @Update("update notifications set read_at = current_timestamp where id = #{id} and (recipient_user_id is null or recipient_user_id = #{userId})")
    int markNotificationRead(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("""
            insert into notifications (recipient_user_id, title, content, category)
            values (#{recipientUserId}, #{title}, #{content}, #{category})
            """)
    void insertNotification(@Param("recipientUserId") Long recipientUserId, @Param("title") String title, @Param("content") String content, @Param("category") String category);

    @Select("""
            select id,
                   name,
                   integration_type as integrationType,
                   endpoint_url as endpointUrl,
                   secret_mask as secretMask,
                   enabled,
                   updated_at as updatedAt
            from external_integrations
            order by id desc
            """)
    List<Map<String, Object>> findIntegrations();

    @Insert("""
            insert into external_integrations (name, integration_type, endpoint_url, secret_mask, enabled, updated_by)
            values (#{name}, #{integrationType}, #{endpointUrl}, #{secretMask}, #{enabled}, #{actorUserId})
            """)
    void insertIntegration(
            @Param("name") String name,
            @Param("integrationType") String integrationType,
            @Param("endpointUrl") String endpointUrl,
            @Param("secretMask") String secretMask,
            @Param("enabled") Boolean enabled,
            @Param("actorUserId") Long actorUserId
    );

    @Select("""
            select id,
                   name,
                   integration_type as integrationType,
                   endpoint_url as endpointUrl,
                   secret_mask as secretMask,
                   enabled,
                   updated_at as updatedAt
            from external_integrations
            where name = #{name}
              and integration_type = #{integrationType}
              and endpoint_url = #{endpointUrl}
            order by id desc
            limit 1
            """)
    Map<String, Object> findLatestIntegrationByIdentity(
            @Param("name") String name,
            @Param("integrationType") String integrationType,
            @Param("endpointUrl") String endpointUrl
    );


    @Update("""
            update external_integrations
            set name = #{name},
                integration_type = #{integrationType},
                endpoint_url = #{endpointUrl},
                secret_mask = #{secretMask},
                enabled = #{enabled},
                updated_by = #{actorUserId},
                updated_at = current_timestamp
            where id = #{id}
            """)
    int updateIntegration(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("integrationType") String integrationType,
            @Param("endpointUrl") String endpointUrl,
            @Param("secretMask") String secretMask,
            @Param("enabled") Boolean enabled,
            @Param("actorUserId") Long actorUserId
    );

    @Select("select count(*) from external_integrations where id = #{id}")
    int countIntegration(@Param("id") Long id);

    @Insert("""
            insert into external_integration_events (integration_id, event_type, status, payload_summary, error_message)
            values (#{integrationId}, #{eventType}, #{status}, #{payloadSummary}, #{errorMessage})
            """)
    void insertIntegrationEvent(
            @Param("integrationId") Long integrationId,
            @Param("eventType") String eventType,
            @Param("status") String status,
            @Param("payloadSummary") String payloadSummary,
            @Param("errorMessage") String errorMessage
    );

    @Select("""
            select id,
                   integration_id as integrationId,
                   event_type as eventType,
                   status,
                   payload_summary as payloadSummary,
                   error_message as errorMessage,
                   created_at as createdAt
            from external_integration_events
            order by id desc
            limit 100
            """)
    List<Map<String, Object>> findIntegrationEvents();
}
