package com.kaoshi.auth.password;

import com.kaoshi.auth.audit.AuditEventService;
import com.kaoshi.auth.dto.ResetPasswordRequest;
import com.kaoshi.auth.registration.VerificationCodeService;
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
public class PasswordResetService {
    private final VerificationCodeService verificationCodeService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventService auditEventService;

    public PasswordResetService(
            VerificationCodeService verificationCodeService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditEventService auditEventService
    ) {
        this.verificationCodeService = verificationCodeService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public void reset(ResetPasswordRequest request, HttpServletRequest servletRequest) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "两次输入的新密码不一致");
        }
        String email = verificationCodeService.normalizeEmail(request.email());
        UserAccount user = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "邮箱未绑定任何账号"));
        verificationCodeService.consume(email, VerificationCodeService.PURPOSE_RESET_PASSWORD, request.code(), servletRequest);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        auditEventService.recordSystem("AUTH_PASSWORD_RESET_COMPLETED", "USER", user.getId(), user.getUsername(), servletRequest, Map.of("email", email));
    }
}
