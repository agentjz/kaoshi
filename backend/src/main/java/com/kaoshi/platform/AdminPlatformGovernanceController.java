package com.kaoshi.platform;

import com.kaoshi.common.api.ApiResponse;
import com.kaoshi.platform.dto.ExternalIntegrationEventResponse;
import com.kaoshi.platform.dto.ExternalIntegrationRequest;
import com.kaoshi.platform.dto.ExternalIntegrationResponse;
import com.kaoshi.platform.dto.NotificationResponse;
import com.kaoshi.security.AuthUser;
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
@RequestMapping("/api/admin/platform")
@PreAuthorize("hasAuthority('system:admin')")
public class AdminPlatformGovernanceController {
    private final PlatformGovernanceService service;

    public AdminPlatformGovernanceController(PlatformGovernanceService service) {
        this.service = service;
    }

    @GetMapping("/notifications")
    public ApiResponse<List<NotificationResponse>> notifications(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(service.notifications(user.id()));
    }

    @PostMapping("/notifications/{id}/read")
    public ApiResponse<Void> markNotificationRead(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        service.markNotificationRead(id, user.id());
        return ApiResponse.ok(null);
    }

    @GetMapping("/integrations")
    public ApiResponse<List<ExternalIntegrationResponse>> integrations() {
        return ApiResponse.ok(service.integrations());
    }

    @PostMapping("/integrations")
    public ApiResponse<ExternalIntegrationResponse> createIntegration(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ExternalIntegrationRequest request) {
        return ApiResponse.ok(service.createIntegration(user.id(), request));
    }

    @PutMapping("/integrations/{id}")
    public ApiResponse<ExternalIntegrationResponse> updateIntegration(@PathVariable Long id, @AuthenticationPrincipal AuthUser user, @Valid @RequestBody ExternalIntegrationRequest request) {
        return ApiResponse.ok(service.updateIntegration(id, user.id(), request));
    }

    @PostMapping("/integrations/{id}/test")
    public ApiResponse<List<ExternalIntegrationEventResponse>> testIntegration(@PathVariable Long id) {
        return ApiResponse.ok(service.testIntegration(id));
    }

    @GetMapping("/integration-events")
    public ApiResponse<List<ExternalIntegrationEventResponse>> integrationEvents() {
        return ApiResponse.ok(service.integrationEvents());
    }
}
