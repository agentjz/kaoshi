package com.kaoshi.auth;

import com.kaoshi.auth.dto.MailStatusResponse;
import com.kaoshi.auth.dto.RegistrationRequestResponse;
import com.kaoshi.auth.dto.RegistrationSettingsRequest;
import com.kaoshi.auth.dto.RegistrationSettingsResponse;
import com.kaoshi.auth.dto.RejectRegistrationRequest;
import com.kaoshi.auth.dto.TestMailRequest;
import com.kaoshi.auth.mail.MailService;
import com.kaoshi.auth.registration.AdminRegistrationService;
import com.kaoshi.auth.registration.RegistrationSettingsService;
import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.security.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auth")
@PreAuthorize("hasAuthority('system:admin') or hasAuthority('system:settings') or hasAuthority('system:users')")
public class AdminAuthController {
    private final RegistrationSettingsService registrationSettingsService;
    private final AdminRegistrationService adminRegistrationService;
    private final MailService mailService;

    public AdminAuthController(
            RegistrationSettingsService registrationSettingsService,
            AdminRegistrationService adminRegistrationService,
            MailService mailService
    ) {
        this.registrationSettingsService = registrationSettingsService;
        this.adminRegistrationService = adminRegistrationService;
        this.mailService = mailService;
    }

    @GetMapping("/registration-settings")
    public ApiResponse<RegistrationSettingsResponse> settings() {
        return ApiResponse.ok(registrationSettingsService.current());
    }

    @PutMapping("/registration-settings")
    public ApiResponse<RegistrationSettingsResponse> updateSettings(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody RegistrationSettingsRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(registrationSettingsService.update(request, user, servletRequest));
    }

    @GetMapping("/mail-status")
    public ApiResponse<MailStatusResponse> mailStatus() {
        return ApiResponse.ok(mailService.status());
    }

    @PostMapping("/test-mail")
    public ApiResponse<Void> testMail(@Valid @RequestBody TestMailRequest request) {
        mailService.sendTestMail(request.email());
        return ApiResponse.ok();
    }

    @GetMapping("/registration-requests")
    public ApiResponse<List<RegistrationRequestResponse>> requests() {
        return ApiResponse.ok(adminRegistrationService.pendingRequests());
    }

    @PostMapping("/registration-requests/{userId}/approve")
    public ApiResponse<RegistrationRequestResponse> approve(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthUser user,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(adminRegistrationService.approve(userId, user, servletRequest));
    }

    @PostMapping("/registration-requests/{userId}/reject")
    public ApiResponse<RegistrationRequestResponse> reject(
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody(required = false) RejectRegistrationRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(adminRegistrationService.reject(userId, request == null ? "" : request.reason(), user, servletRequest));
    }
}
