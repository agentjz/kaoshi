package com.kaoshi.auth.registration;

import com.kaoshi.auth.audit.AuditEventService;
import com.kaoshi.auth.dto.RegistrationRequestResponse;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.security.AuthUser;
import com.kaoshi.user.domain.UserAccount;
import com.kaoshi.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminRegistrationService {
    private final AdminRegistrationMapper mapper;
    private final UserMapper userMapper;
    private final AuditEventService auditEventService;

    public AdminRegistrationService(AdminRegistrationMapper mapper, UserMapper userMapper, AuditEventService auditEventService) {
        this.mapper = mapper;
        this.userMapper = userMapper;
        this.auditEventService = auditEventService;
    }

    public List<RegistrationRequestResponse> pendingRequests() {
        return mapper.findByApprovalStatus("PENDING").stream().map(this::toResponse).toList();
    }

    @Transactional
    public RegistrationRequestResponse approve(Long userId, AuthUser actor, HttpServletRequest request) {
        if (mapper.approve(userId) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "注册申请不存在或状态已变化");
        }
        UserAccount user = userMapper.selectById(userId);
        auditEventService.record(actor, "AUTH_REGISTRATION_APPROVED", "USER", userId, user == null ? null : user.getUsername(), request, Map.of());
        return toResponse(user);
    }

    @Transactional
    public RegistrationRequestResponse reject(Long userId, String reason, AuthUser actor, HttpServletRequest request) {
        if (mapper.reject(userId) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "注册申请不存在或状态已变化");
        }
        UserAccount user = userMapper.selectById(userId);
        auditEventService.record(actor, "AUTH_REGISTRATION_REJECTED", "USER", userId, user == null ? null : user.getUsername(), request, Map.of("reason", reason == null ? "" : reason));
        return toResponse(user);
    }

    private RegistrationRequestResponse toResponse(UserAccount user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return new RegistrationRequestResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getStatus(),
                user.getApprovalStatus(),
                user.getRegisteredAt()
        );
    }
}
