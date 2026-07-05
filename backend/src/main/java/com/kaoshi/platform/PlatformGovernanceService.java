package com.kaoshi.platform;

import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.platform.dto.ExternalIntegrationEventResponse;
import com.kaoshi.platform.dto.ExternalIntegrationRequest;
import com.kaoshi.platform.dto.ExternalIntegrationResponse;
import com.kaoshi.platform.dto.NotificationResponse;
import com.kaoshi.platform.mapper.PlatformGovernanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.kaoshi.exam.ExamRowValues.booleanValue;
import static com.kaoshi.exam.ExamRowValues.dateTimeValue;
import static com.kaoshi.exam.ExamRowValues.dateTimeValueOrNull;
import static com.kaoshi.exam.ExamRowValues.longValue;
import static com.kaoshi.exam.ExamRowValues.stringValue;
import static com.kaoshi.exam.ExamRowValues.value;

@Service
public class PlatformGovernanceService {
    private final PlatformGovernanceMapper mapper;

    public PlatformGovernanceService(PlatformGovernanceMapper mapper) {
        this.mapper = mapper;
    }

    public List<NotificationResponse> notifications(Long userId) {
        return mapper.findNotifications(userId).stream().map(this::toNotification).toList();
    }

    public void markNotificationRead(Long id, Long userId) {
        if (mapper.markNotificationRead(id, userId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "通知不存在");
        }
    }

    public void notifyAll(String title, String content, String category) {
        mapper.insertNotification(null, title, content, category);
    }

    public List<ExternalIntegrationResponse> integrations() {
        return mapper.findIntegrations().stream().map(this::toIntegration).toList();
    }

    @Transactional
    public ExternalIntegrationResponse createIntegration(Long actorUserId, ExternalIntegrationRequest request) {
        String name = request.name().trim();
        String integrationType = request.integrationType().trim();
        String endpointUrl = request.endpointUrl().trim();
        mapper.insertIntegration(name, integrationType, endpointUrl, mask(request.secretMask()), Boolean.TRUE.equals(request.enabled()), actorUserId);
        notifyAll("外部集成已创建", "已创建外部集成：" + name, "INTEGRATION");
        Map<String, Object> created = mapper.findLatestIntegrationByIdentity(name, integrationType, endpointUrl);
        if (created == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "外部集成创建后读回失败");
        }
        return toIntegration(created);
    }

    @Transactional
    public ExternalIntegrationResponse updateIntegration(Long id, Long actorUserId, ExternalIntegrationRequest request) {
        if (mapper.updateIntegration(id, request.name().trim(), request.integrationType().trim(), request.endpointUrl().trim(), mask(request.secretMask()), Boolean.TRUE.equals(request.enabled()), actorUserId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "外部集成不存在");
        }
        notifyAll("外部集成已更新", "已更新外部集成：" + request.name().trim(), "INTEGRATION");
        return integrations().stream()
                .filter(item -> id.equals(item.id()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "外部集成不存在"));
    }

    @Transactional
    public List<ExternalIntegrationEventResponse> testIntegration(Long id) {
        if (mapper.countIntegration(id) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "外部集成不存在");
        }
        mapper.insertIntegrationEvent(id, "TEST", "QUEUED", "管理员触发测试事件，等待外部系统消费。", null);
        notifyAll("外部集成测试事件已记录", "外部集成测试事件已写入投递日志。", "INTEGRATION");
        return integrationEvents();
    }

    public List<ExternalIntegrationEventResponse> integrationEvents() {
        return mapper.findIntegrationEvents().stream().map(this::toIntegrationEvent).toList();
    }

    private NotificationResponse toNotification(Map<String, Object> row) {
        return new NotificationResponse(
                longValue(value(row, "id")),
                longValueOrNull(value(row, "recipientUserId")),
                stringValue(value(row, "title")),
                stringValue(value(row, "content")),
                stringValue(value(row, "category")),
                dateTimeValueOrNull(value(row, "readAt")) != null,
                dateTimeValue(value(row, "createdAt"))
        );
    }

    private ExternalIntegrationResponse toIntegration(Map<String, Object> row) {
        return new ExternalIntegrationResponse(
                longValue(value(row, "id")),
                stringValue(value(row, "name")),
                stringValue(value(row, "integrationType")),
                stringValue(value(row, "endpointUrl")),
                stringValue(value(row, "secretMask")),
                booleanValue(value(row, "enabled")),
                dateTimeValue(value(row, "updatedAt"))
        );
    }

    private ExternalIntegrationEventResponse toIntegrationEvent(Map<String, Object> row) {
        return new ExternalIntegrationEventResponse(
                longValue(value(row, "id")),
                longValue(value(row, "integrationId")),
                stringValue(value(row, "eventType")),
                stringValue(value(row, "status")),
                stringValue(value(row, "payloadSummary")),
                stringValue(value(row, "errorMessage")),
                dateTimeValue(value(row, "createdAt"))
        );
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("****")) {
            return trimmed;
        }
        return "****" + trimmed.substring(Math.max(0, trimmed.length() - 4));
    }

    private Long longValueOrNull(Object value) {
        return value == null ? null : longValue(value);
    }
}
