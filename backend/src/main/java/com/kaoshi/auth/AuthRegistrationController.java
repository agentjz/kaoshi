package com.kaoshi.auth;

import com.kaoshi.auth.dto.MailStatusResponse;
import com.kaoshi.auth.dto.PasswordResetCodeRequest;
import com.kaoshi.auth.dto.RegisterRequest;
import com.kaoshi.auth.dto.RegisterResponse;
import com.kaoshi.auth.dto.RegistrationSettingsResponse;
import com.kaoshi.auth.dto.ResetPasswordRequest;
import com.kaoshi.auth.dto.VerificationCodeRequest;
import com.kaoshi.auth.dto.VerificationCodeResponse;
import com.kaoshi.auth.password.PasswordResetService;
import com.kaoshi.auth.registration.AuthRegistrationService;
import com.kaoshi.auth.registration.RegistrationSettingsService;
import com.kaoshi.auth.registration.VerificationCodeService;
import com.kaoshi.auth.mail.MailService;
import com.kaoshi.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRegistrationController {
    private final RegistrationSettingsService registrationSettingsService;
    private final VerificationCodeService verificationCodeService;
    private final AuthRegistrationService registrationService;
    private final PasswordResetService passwordResetService;
    private final MailService mailService;

    public AuthRegistrationController(
            RegistrationSettingsService registrationSettingsService,
            VerificationCodeService verificationCodeService,
            AuthRegistrationService registrationService,
            PasswordResetService passwordResetService,
            MailService mailService
    ) {
        this.registrationSettingsService = registrationSettingsService;
        this.verificationCodeService = verificationCodeService;
        this.registrationService = registrationService;
        this.passwordResetService = passwordResetService;
        this.mailService = mailService;
    }

    @GetMapping("/registration-settings")
    public ApiResponse<RegistrationSettingsResponse> registrationSettings() {
        return ApiResponse.ok(registrationSettingsService.current());
    }

    @GetMapping("/mail-status")
    public ApiResponse<MailStatusResponse> mailStatus() {
        return ApiResponse.ok(mailService.status());
    }

    @PostMapping("/verification-codes")
    public ApiResponse<VerificationCodeResponse> verificationCode(@Valid @RequestBody VerificationCodeRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok(verificationCodeService.send(request.email(), request.purpose(), servletRequest));
    }

    @PostMapping("/password-reset-codes")
    public ApiResponse<VerificationCodeResponse> passwordResetCode(@Valid @RequestBody PasswordResetCodeRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok(verificationCodeService.send(request.email(), VerificationCodeService.PURPOSE_RESET_PASSWORD, servletRequest));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok(registrationService.register(request, servletRequest));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest servletRequest) {
        passwordResetService.reset(request, servletRequest);
        return ApiResponse.ok();
    }
}
