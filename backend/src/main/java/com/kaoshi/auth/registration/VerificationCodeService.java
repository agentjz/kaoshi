package com.kaoshi.auth.registration;

import com.kaoshi.auth.audit.AuditEventService;
import com.kaoshi.auth.dto.RegistrationSettingsResponse;
import com.kaoshi.auth.dto.VerificationCodeResponse;
import com.kaoshi.auth.mail.MailService;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Service
public class VerificationCodeService {
    public static final String PURPOSE_REGISTER = "REGISTER";
    public static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";

    private static final int CODE_TTL_MINUTES = 10;
    private static final int RESEND_INTERVAL_SECONDS = 60;
    private static final int LOCK_MINUTES = 15;

    private final EmailVerificationCodeMapper mapper;
    private final RegistrationSettingsService settingsService;
    private final MailService mailService;
    private final AuditEventService auditEventService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationCodeService(
            EmailVerificationCodeMapper mapper,
            RegistrationSettingsService settingsService,
            MailService mailService,
            AuditEventService auditEventService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.mapper = mapper;
        this.settingsService = settingsService;
        this.mailService = mailService;
        this.auditEventService = auditEventService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public VerificationCodeResponse send(String rawEmail, String rawPurpose, HttpServletRequest request) {
        String email = normalizeEmail(rawEmail);
        String purpose = normalizePurpose(rawPurpose);
        if (PURPOSE_REGISTER.equals(purpose)) {
            ensureRegisterCodeAllowed(email);
        } else if (PURPOSE_RESET_PASSWORD.equals(purpose) && userMapper.findByEmail(email).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮箱未绑定任何账号");
        }
        EmailVerificationCode latest = mapper.findLatest(email, purpose);
        LocalDateTime now = LocalDateTime.now();
        if (latest != null && latest.getLastSentAt() != null && latest.getLastSentAt().isAfter(now.minusSeconds(RESEND_INTERVAL_SECONDS))) {
            throw new BusinessException(ErrorCode.CONFLICT, "验证码发送过于频繁，请稍后再试");
        }
        String code = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime expiresAt = now.plusMinutes(CODE_TTL_MINUTES);
        mapper.insert(new EmailVerificationCodeMapper.CodeInsertRow(
                email,
                purpose,
                passwordEncoder.encode(code),
                expiresAt,
                clientIp(request),
                request == null ? null : request.getHeader("User-Agent")
        ));
        mailService.sendVerificationCode(email, purpose, code);
        auditEventService.recordSystem("AUTH_VERIFICATION_SENT", "EMAIL", email, email, request, Map.of("purpose", purpose));
        return new VerificationCodeResponse(email, purpose, expiresAt, mailService.exposedCode(code));
    }

    @Transactional
    public void consume(String rawEmail, String rawPurpose, String code, HttpServletRequest request) {
        String email = normalizeEmail(rawEmail);
        String purpose = normalizePurpose(rawPurpose);
        EmailVerificationCode latest = mapper.findLatest(email, purpose);
        LocalDateTime now = LocalDateTime.now();
        if (latest == null || latest.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "验证码无效或已过期");
        }
        if (latest.getLockedUntil() != null && latest.getLockedUntil().isAfter(now)) {
            throw new BusinessException(ErrorCode.CONFLICT, "验证码错误次数过多，请稍后再试");
        }
        if (!passwordEncoder.matches(code, latest.getCodeHash())) {
            mapper.recordFailure(latest.getId(), now.plusMinutes(LOCK_MINUTES));
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "验证码不正确");
        }
        if (mapper.consume(latest.getId()) == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "验证码已使用");
        }
        auditEventService.recordSystem("AUTH_EMAIL_VERIFIED", "EMAIL", email, email, request, Map.of("purpose", purpose));
    }

    public String normalizeEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "邮箱不能为空");
        }
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizePurpose(String rawPurpose) {
        String purpose = rawPurpose == null ? "" : rawPurpose.trim().toUpperCase(Locale.ROOT);
        if (!PURPOSE_REGISTER.equals(purpose) && !PURPOSE_RESET_PASSWORD.equals(purpose)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "验证码用途不支持");
        }
        return purpose;
    }

    private void ensureRegisterCodeAllowed(String email) {
        RegistrationSettingsResponse settings = settingsService.current();
        if (!settings.selfRegistrationEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前部署未开放自助注册");
        }
        settingsService.ensureDomainAllowed(email);
        if (userMapper.countByEmail(email) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已注册");
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
