package com.kaoshi.auth.registration;

import com.kaoshi.auth.audit.AuditEventService;
import com.kaoshi.auth.dto.RegisterRequest;
import com.kaoshi.auth.dto.RegisterResponse;
import com.kaoshi.auth.dto.RegistrationSettingsResponse;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.user.domain.UserAccount;
import com.kaoshi.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthRegistrationService {
    private final RegistrationSettingsService settingsService;
    private final VerificationCodeService verificationCodeService;
    private final AuditEventService auditEventService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthRegistrationService(
            RegistrationSettingsService settingsService,
            VerificationCodeService verificationCodeService,
            AuditEventService auditEventService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.settingsService = settingsService;
        this.verificationCodeService = verificationCodeService;
        this.auditEventService = auditEventService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request, HttpServletRequest servletRequest) {
        RegistrationSettingsResponse settings = settingsService.current();
        if (!settings.selfRegistrationEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前部署未开放自助注册");
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "两次输入的密码不一致");
        }
        String email = verificationCodeService.normalizeEmail(request.email());
        settingsService.ensureDomainAllowed(email);
        if (userMapper.countByUsername(request.username()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "账号已存在");
        }
        if (userMapper.countByEmail(email) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已注册");
        }
        if (settings.emailVerificationRequired()) {
            if (request.verificationCode() == null || request.verificationCode().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "请输入邮箱验证码");
            }
            verificationCodeService.consume(email, VerificationCodeService.PURPOSE_REGISTER, request.verificationCode(), servletRequest);
        }
        Long roleId = userMapper.findRoleIdByCode(settings.defaultRoleCode());
        if (roleId == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "默认注册角色未配置");
        }
        String approvalStatus = settings.adminApprovalRequired() ? "PENDING" : "APPROVED";
        UserAccount user = new UserAccount();
        user.setDepartmentId(settings.defaultDepartmentId());
        user.setUsername(request.username().trim());
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus("ACTIVE");
        user.setRegistrationSource("SELF_REGISTERED");
        user.setApprovalStatus(approvalStatus);
        user.setRegisteredAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        userMapper.insert(user);
        userMapper.insertUserRole(user.getId(), roleId);
        auditEventService.recordSystem("AUTH_REGISTER_REQUESTED", "USER", user.getId(), user.getUsername(), servletRequest, Map.of(
                "email", email,
                "approvalStatus", approvalStatus
        ));
        String message = "APPROVED".equals(approvalStatus) ? "注册成功，请登录" : "注册成功，等待管理员审核";
        return new RegisterResponse(user.getId(), user.getUsername(), email, true, approvalStatus, message);
    }
}
