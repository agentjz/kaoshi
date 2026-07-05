package com.kaoshi.auth.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaoshi.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditEventService {
    private final AuditEventMapper mapper;
    private final ObjectMapper objectMapper;

    public AuditEventService(AuditEventMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public void record(
            AuthUser actor,
            String action,
            String resourceType,
            Object resourceId,
            String resourceTitle,
            HttpServletRequest request,
            Object payload
    ) {
        mapper.insert(
                actor == null ? null : actor.id(),
                actor == null ? null : actor.username(),
                action,
                resourceType,
                resourceId == null ? null : String.valueOf(resourceId),
                resourceTitle,
                clientIp(request),
                request == null ? null : request.getHeader("User-Agent"),
                toJson(payload)
        );
    }

    public void recordSystem(String action, String resourceType, Object resourceId, String resourceTitle, HttpServletRequest request, Object payload) {
        record(null, action, resourceType, resourceId, resourceTitle, request, payload);
    }

    private String toJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (JsonProcessingException exception) {
            return "{\"serialization\":\"failed\"}";
        }
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
