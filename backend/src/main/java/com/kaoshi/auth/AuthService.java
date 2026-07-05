package com.kaoshi.auth;

import com.kaoshi.auth.dto.CurrentUserResponse;
import com.kaoshi.auth.dto.LoginRequest;
import com.kaoshi.auth.dto.LoginResponse;
import com.kaoshi.auth.dto.ChangePasswordRequest;
import com.kaoshi.auth.audit.AuditEventService;
import com.kaoshi.common.api.ErrorCode;
import com.kaoshi.common.exception.BusinessException;
import com.kaoshi.security.AuthUser;
import com.kaoshi.security.JwtProperties;
import com.kaoshi.security.JwtService;
import com.kaoshi.user.domain.UserAccount;
import com.kaoshi.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventService auditEventService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditEventService auditEventService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditEventService = auditEventService;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException exception) {
            auditEventService.recordSystem("AUTH_LOGIN_FAILED", "USER", request.username(), request.username(), servletRequest, Map.of("reason", exception.getClass().getSimpleName()));
            throw exception;
        }
        AuthUser user = (AuthUser) authentication.getPrincipal();
        UserAccount account = userMapper.selectById(user.id());
        if (account != null) {
            account.setLastLoginAt(LocalDateTime.now());
            userMapper.updateById(account);
        }
        return new LoginResponse(
                jwtService.issue(user),
                "Bearer",
                jwtProperties.accessTokenMinutes() * 60,
                toCurrentUser(user)
        );
    }

    public CurrentUserResponse toCurrentUser(AuthUser user) {
        return new CurrentUserResponse(
                user.id(),
                user.username(),
                user.displayName(),
                Boolean.TRUE.equals(user.mustChangePassword()),
                user.roles(),
                user.permissions()
        );
    }

    public void changePassword(AuthUser authUser, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "两次输入的新密码不一致");
        }
        UserAccount user = userMapper.selectById(authUser.id());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "当前密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userMapper.updateById(user);
    }
}

