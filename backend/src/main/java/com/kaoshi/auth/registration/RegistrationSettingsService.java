package com.kaoshi.auth.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaoshi.auth.audit.AuditEventService;
import com.kaoshi.auth.dto.RegistrationSettingsRequest;
import com.kaoshi.auth.dto.RegistrationSettingsResponse;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.security.AuthUser;
import com.kaoshi.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationSettingsService {
    private static final String CONFIG_KEY = "auth.registration.settings";
    private static final RegistrationSettingsResponse DEFAULT_SETTINGS = new RegistrationSettingsResponse(
            true,
            true,
            false,
            "STUDENT",
            2L,
            List.of(),
            ""
    );

    private final RegistrationSettingsMapper mapper;
    private final UserMapper userMapper;
    private final AuditEventService auditEventService;
    private final ObjectMapper objectMapper;

    public RegistrationSettingsService(
            RegistrationSettingsMapper mapper,
            UserMapper userMapper,
            AuditEventService auditEventService,
            ObjectMapper objectMapper
    ) {
        this.mapper = mapper;
        this.userMapper = userMapper;
        this.auditEventService = auditEventService;
        this.objectMapper = objectMapper;
    }

    public RegistrationSettingsResponse current() {
        String value = mapper.findValue(CONFIG_KEY);
        if (value == null || value.isBlank()) {
            return DEFAULT_SETTINGS;
        }
        try {
            RegistrationSettingsResponse settings = objectMapper.readValue(value, RegistrationSettingsResponse.class);
            return normalize(settings);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "注册策略配置无法解析");
        }
    }

    @Transactional
    public RegistrationSettingsResponse update(RegistrationSettingsRequest request, AuthUser actor, HttpServletRequest servletRequest) {
        Long roleId = userMapper.findRoleIdByCode(request.defaultRoleCode());
        if (roleId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "默认角色不存在");
        }
        RegistrationSettingsResponse settings = normalize(new RegistrationSettingsResponse(
                request.selfRegistrationEnabled(),
                request.emailVerificationRequired(),
                request.adminApprovalRequired(),
                request.defaultRoleCode(),
                request.defaultDepartmentId(),
                request.allowedEmailDomains(),
                request.termsText()
        ));
        String value = toJson(settings);
        if (mapper.updateValue(CONFIG_KEY, value) == 0) {
            mapper.insertValue(CONFIG_KEY, value, "注册策略");
        }
        auditEventService.record(actor, "AUTH_REGISTRATION_SETTINGS_UPDATED", "SYSTEM_CONFIG", CONFIG_KEY, "注册策略", servletRequest, settings);
        return settings;
    }

    public void ensureDomainAllowed(String email) {
        List<String> domains = current().allowedEmailDomains();
        if (domains.isEmpty()) {
            return;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        if (domains.stream().noneMatch(item -> item.equalsIgnoreCase(domain))) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "当前邮箱域名不允许自助注册");
        }
    }

    private RegistrationSettingsResponse normalize(RegistrationSettingsResponse settings) {
        return new RegistrationSettingsResponse(
                settings.selfRegistrationEnabled(),
                settings.emailVerificationRequired(),
                settings.adminApprovalRequired(),
                settings.defaultRoleCode() == null || settings.defaultRoleCode().isBlank() ? "STUDENT" : settings.defaultRoleCode().trim(),
                settings.defaultDepartmentId(),
                settings.allowedEmailDomains() == null ? List.of() : settings.allowedEmailDomains().stream()
                        .filter(item -> item != null && !item.isBlank())
                        .map(item -> item.trim().toLowerCase())
                        .distinct()
                        .toList(),
                settings.termsText() == null ? "" : settings.termsText()
        );
    }

    private String toJson(RegistrationSettingsResponse settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "注册策略配置无法保存");
        }
    }
}
